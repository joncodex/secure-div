package com.enzelascripts.securediv;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

public class PlaywrightTest {

    private static Playwright playwright;
    private static Browser browser;

    @BeforeAll
    static void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void tearDown() {
        browser.close();
        playwright.close();
    }

    @Test
    void exampleTest() {
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        page.navigate("https://example.com");
        System.out.println(page.title());
        context.close();
    }

    @Test
    void chromiumOnlyTest() {
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        page.navigate("https://playwright.dev");
        System.out.println("Title: " + page.title());
        context.close();
    }
}