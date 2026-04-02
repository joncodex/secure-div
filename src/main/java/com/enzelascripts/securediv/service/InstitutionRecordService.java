package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.request.InstitutionRecordRequest;
import com.enzelascripts.securediv.response.InstitutionResponse;
import com.enzelascripts.securediv.entity.InstitutionRecord;
import com.enzelascripts.securediv.exception.ResourceNotFoundException;
import com.enzelascripts.securediv.repository.InstitutionRecordRepo;
import jakarta.persistence.LockModeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.enzelascripts.securediv.util.Utility.*;

@Slf4j
@Service
public class InstitutionRecordService {

///  ============================================== Fields ==================================================
    @Autowired
    private InstitutionRecordRepo institutionRecordRepo;


///  ============================================== Public Methods ==========================================
    @Transactional(timeout = 5)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Map<String, Object> createInstitutionRecord(InstitutionRecordRequest dto) {

        InstitutionRecord newInstitutionRecord =
                transferData(validateNotNull(dto), new InstitutionRecord());

        String logoUrl = convertToBase64ImageUrl(dto.getLogoImage());
        newInstitutionRecord.setLogoUrl(logoUrl);

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

        institutionRecordRepo.save(newInstitutionRecord);

        InstitutionResponse response = transferData(newInstitutionRecord, new InstitutionResponse());

        return Map.of(
                "message", "Institution Record created successfully",
                "data", response
        );

    }

    public InstitutionRecord getCurrentInstitutionRecord() {

        return  institutionRecordRepo.findInstitutionRecordByCurrent(true).orElseThrow(
                () -> new ResourceNotFoundException("No valid Institution Record at the moment")
        );
    }


// ========================================= helper Methods  =====================================================

    private List<InstitutionRecord> getCurrentRecords() {

        return institutionRecordRepo.getInstitutionRecordsByCurrent(true);
    }


}
