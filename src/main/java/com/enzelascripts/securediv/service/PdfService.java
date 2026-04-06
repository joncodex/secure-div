package com.enzelascripts.securediv.service;

import com.microsoft.playwright.*;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PdfService {

    private Playwright playwright;
    private Browser browser;

    public byte[] generatePdf(String html) {
        try {
            Page page = getBrowser().newPage();
            page.setContent(html);

            byte[] pdf = page.pdf(new Page.PdfOptions()
                    .setFormat("A4")
                    .setPrintBackground(true)
            );

            page.close();
            return pdf;

        } catch (Exception e) {
            throw new RuntimeException("Unable to generate PDF", e);
        }
    }

    private synchronized Browser getBrowser() {
        if (playwright == null) {
            Map<String, String> env = Map.of(
                    "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1",
                    "PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS", "1"
            );
            playwright = Playwright.create(new Playwright.CreateOptions().setEnv(env));
        }

        if (browser == null) {
            browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );
        }

        return browser;
    }

    @PreDestroy
    public void closeBrowser() {

        //closes resources gracefully
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}