package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.Signatory;
import com.enzelascripts.securediv.exception.ResourceNotFoundException;
import com.enzelascripts.securediv.repository.SignatoryRepo;
import com.enzelascripts.securediv.request.SignatoryRequest;
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
import static com.enzelascripts.securediv.util.Utility.transferData;

@Slf4j
@Service
public class SignatoryService {

///  ============================================== Fields ==================================================
    @Autowired
    private SignatoryRepo signatoryRepo;
    @Autowired
    private S3Service s3Service;

    ///  ============================================== Public Methods ==========================================
    @Transactional(timeout = 5)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Map<String, Object> createSignatory(SignatoryRequest dto) {

        Signatory signatory =
                transferData(validateNotNull(dto), new Signatory());

        //create s3 key
        String specialName = dto.getName() + "_" + LocalDate.now();
        String folderName = "signatures";
        String extension = Objects.requireNonNull(dto.getSignatureImage().getContentType()).split("/")[1];

        String s3Key = folderName + "/" + specialName + "." + extension;

        //upload the signature file to s3
        MultipartFile signatureImage = dto.getSignatureImage();
        byte[] fileByte = getFileBytes(signatureImage);
        s3Service.uploadSignature(fileByte, s3Key);

        //update the signatory object
        signatory.setS3Key(s3Key);
        signatory.setCreatedAt(LocalDate.now());
        signatory.setCurrent(true);

        signatoryRepo.save(signatory);

        SignatoryResponse response = SignatoryResponse.builder()
                .name(signatory.getName())
                .position(signatory.getPosition())
                .signatureUrl(convertToBase64ImageUrl(signatureImage))
                .build();

        return Map.of(
                "message", "Signatory created successfully",
                "data", response
        );

    }

    public List<SignatoryResponse> getCurrentSignatories() {

        List<Signatory> signatories = signatoryRepo.findSignatoriesByCurrent(true);
        if(signatories.isEmpty()){
            throw new ResourceNotFoundException("No valid Signatory at the moment");
        }

        return signatories.stream()
                .map(this::getSignatoryResponse)
                .toList();
    }

    public SignatoryResponse getSignatoryResponse(Signatory s) {

        return SignatoryResponse.builder()
                .name(s.getName())
                .position(s.getPosition())
                .signatureUrl(getSignatureImgUrl(s.getS3Key()))
                .build();
    }

    public void invalidateSignatories(String name) {
        Signatory signatory = signatoryRepo.findSignatoryByName(name).orElseThrow(() ->
                new ResourceNotFoundException("Signatory with name: " + name + " not found"));

        signatory.setCurrent(false);
        signatoryRepo.save(signatory);
    }


// ========================================= helper Methods  =====================================================
    private String getSignatureImgUrl(String s3Key){

        byte[] signatureBytes = s3Service.getSignatureAsBytes(s3Key);
        String extension = s3Key.substring(s3Key.lastIndexOf('.'));

        return  "data:" + "image/" + extension + ";base64,"
                + Base64.getEncoder().encodeToString(signatureBytes);
    }

}
