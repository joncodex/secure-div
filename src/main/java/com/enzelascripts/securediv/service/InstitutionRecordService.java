package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.request.InstitutionRecordRequest;
import com.enzelascripts.securediv.response.InstitutionResponse;
import com.enzelascripts.securediv.entity.InstitutionRecord;
import com.enzelascripts.securediv.exception.ResourceNotFoundException;
import com.enzelascripts.securediv.repository.InstitutionRecordRepo;
import com.enzelascripts.securediv.response.SignatoryResponse;
import jakarta.persistence.LockModeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.enzelascripts.securediv.util.Utility.*;

@Slf4j
@Service
public class InstitutionRecordService {

//  ============================================== Fields ==================================================
    @Autowired
    private InstitutionRecordRepo institutionRecordRepo;
    @Autowired
    private S3Service s3Service;


//  ============================================== Public Methods ==========================================
    @Transactional(timeout = 5)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Map<String, Object> createInstitutionRecord(InstitutionRecordRequest dto) {

        InstitutionRecord newInstitutionRecord =
                transferData(validateNotNull(dto), new InstitutionRecord());

        //create s3 key
        String specialName = dto.getInstitutionName() + "_" + LocalDate.now();
        String folderName = "logos";
        String extension = Objects.requireNonNull(dto.getLogoImage().getContentType()).split("/")[1];

        String s3Key = folderName + "/" + specialName + "." + extension;

        //upload the signature file to s3
        MultipartFile logoImage = dto.getLogoImage();
        byte[] fileByte = getFileBytes(logoImage);
        s3Service.uploadLogo(fileByte, s3Key);

        //update the institution record object
        newInstitutionRecord.setS3Key(s3Key);
        newInstitutionRecord.setCreatedAt(LocalDate.now());
        newInstitutionRecord.setCurrent(true);

        //ensure that other records are deactivated
        List<InstitutionRecord> currentInstitutionRecords = getCurrentRecords();
        if(!currentInstitutionRecords.isEmpty()){
            currentInstitutionRecords
                    .forEach(record -> {
                        record.setCurrent(false);
                        record.setInvalidatedAt(LocalDate.now());
                    });
            institutionRecordRepo.saveAll(currentInstitutionRecords);
        }

        //save the institution record to DB
        institutionRecordRepo.save(newInstitutionRecord);

        //transfer the institution record object to a response object
        InstitutionResponse response = transferData(newInstitutionRecord, new InstitutionResponse());

        //build the base64 logo url
        String logoUrl = convertToBase64ImageUrl(dto.getLogoImage());

        //update the response object
        response.setLogoUrl(logoUrl);


        return Map.of(
                "message", "Institution Record created successfully",
                "data", response
        );

    }

    public InstitutionResponse getCurrentInstitutionRecord(){

        InstitutionRecord record = findCurrentInstitutionRecord();
        String s3Key = record.getS3Key();

        InstitutionResponse response = transferData(record, new InstitutionResponse());
        response.setLogoUrl(getLogoUrl(s3Key));

        return response;

    }


// ========================================= helper Methods  =====================================================
    private List<InstitutionRecord> getCurrentRecords() {

        return institutionRecordRepo.getInstitutionRecordsByCurrent(true);
    }

    private String getLogoUrl(String s3Key){

        //get the institution logo from s3
        byte[] logoBytes = s3Service.getLogoAsBytes(s3Key);
        String extension = s3Key.substring(s3Key.lastIndexOf('.'));

        return  "data:" + "image/" + extension + ";base64,"
                + Base64.getEncoder().encodeToString(logoBytes);
    }

    private InstitutionRecord findCurrentInstitutionRecord() {

        return  institutionRecordRepo.findInstitutionRecordByCurrent(true).orElseThrow(
                () -> new ResourceNotFoundException("No valid Institution Record at the moment")
        );
    }


}
