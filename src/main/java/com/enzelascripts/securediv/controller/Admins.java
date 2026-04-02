package com.enzelascripts.securediv.controller;

import com.enzelascripts.securediv.entity.Certificate;
import com.enzelascripts.securediv.response.CertificateResponse;
import com.enzelascripts.securediv.service.CertificateService;
import com.enzelascripts.securediv.service.PdfService;
import com.enzelascripts.securediv.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.enzelascripts.securediv.util.Utility.getFileFingerprint;

@Controller
@RequestMapping("/admins/v1")
public class Admins {
    @Autowired
    private CertificateService service;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private PdfService pdfService;


    //show me the certificate
    @GetMapping("/certificates/{certificateNumber}")
    public String show(
            @PathVariable
            String certificateNumber,
            Model model){

        //get the certificate
        Certificate cert =  service.getCertificateByCertificateNumber(certificateNumber);
        CertificateResponse response = service.getCertificateResponseObject(cert);
        System.out.println("I have gotten the certificate.... going to html zone now");

        //populate the HTML
        String html = service.getCertificateHTML(cert);
        System.out.println("I have populated the HTML.... going to convert to bytes now");

        //convert HTML to PDF bytes
        byte[] bytes = pdfService.generatePdf(html);
//        byte[] bytes = service.convertHtmlToBytes(html);
        System.out.println("I have converted the HTML to bytes.... going to upload to S3 now");

        //get the file fingerprint
        String fingerprint = getFileFingerprint(bytes);
        System.out.println("I have got the file fingerprint.... going to upload to S3 now");

        //upload the PDF
        s3Service.upload(bytes, cert.getCertificateNumber());
        System.out.println("I have uploaded the PDF to S3.... going to update the certificate now");

        //update and save the certificate object
        cert.setFingerprint(fingerprint);
        cert.setDownloadUrl(
                s3Service.getDownloadUrl(cert.getCertificateNumber()));
        service.saveCertificate(cert);
        System.out.println("I have updated the certificate.... going to send the email now");

        //send the email to the student to download the certificate
        //perform on the background
        //emailService.send(studentEmail)

        System.out.println("Download url: " + cert.getDownloadUrl());



        model.addAttribute("certificate", response);
        return "certificate";

    }

}
