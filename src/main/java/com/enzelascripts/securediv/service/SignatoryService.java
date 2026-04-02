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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.enzelascripts.securediv.util.Utility.*;
import static com.enzelascripts.securediv.util.Utility.transferData;

@Slf4j
@Service
public class SignatoryService {

///  ============================================== Fields ==================================================
    @Autowired
    private SignatoryRepo signatoryRepo;

///  ============================================== Public Methods ==========================================
    @Transactional(timeout = 5)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Map<String, Object> createSignatory(SignatoryRequest dto) {

        Signatory signatory =
                transferData(validateNotNull(dto), new Signatory());

        String signatureUrl = convertToBase64ImageUrl(dto.getSignatureImage());
        signatory.setSignatureUrl(signatureUrl);

        signatory.setCreatedAt(LocalDate.now());
        signatory.setCurrent(true);

        signatoryRepo.save(signatory);

        SignatoryResponse response = transferData(signatory, new SignatoryResponse());

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

        return signatories
                .stream()
                .map(signatory ->
                transferData(signatory, new SignatoryResponse()))
                .toList();
    }

    public void invalidateSignatories(String name) {
        Signatory signatory = signatoryRepo.findSignatoryByName(name).orElseThrow(() ->
                new ResourceNotFoundException("Signatory with name: " + name + " not found"));

        signatory.setCurrent(false);
        signatoryRepo.save(signatory);
    }


    /// ========================================= helper Methods  =====================================================


}
