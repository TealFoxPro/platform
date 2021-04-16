package com.vaadin.platform.gradle.test;

import static com.vaadin.platform.gradle.test.views.helloview.HelloVaadinerView.FONT_AWESOME_ID;
import static com.vaadin.platform.gradle.test.views.helloview.HelloVaadinerView.TEXT_FIELD_ID;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.ImageElement;
import com.vaadin.flow.component.html.testbench.SpanElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.testbench.TestBenchElement;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import io.github.bonigarcia.wdm.WebDriverManager;

public class HelloVaadinerIT extends AbstractViewTest {

    @BeforeClass
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @Test
    public void clickingButton_showsNotification() {
        Assert.assertFalse($(NotificationElement.class).exists());
        $(ButtonElement.class).first().click();
        Assert.assertTrue($(NotificationElement.class).waitForFirst().isOpen());
    }

    @Test
    public void clickingButtonTwice_showsTwoNotifications() {
        Assert.assertFalse($(NotificationElement.class).exists());
        ButtonElement button = $(ButtonElement.class).first();
        button.click();
        button.click();
        Assert.assertEquals(2, $(NotificationElement.class).all().size());
    }

    @Test
    public void buttonIsUsingLumoTheme() {
        WebElement element = $(ButtonElement.class).first();
        assertThemePresentOnElement(element, Lumo.class);
    }

    @Test
    public void clickButton_showsHelloAnonymousUserNotificationWhenUserNameIsEmpty() {
        ButtonElement button = $(ButtonElement.class).first();
        button.click();
        Assert.assertTrue($(NotificationElement.class).exists());
        NotificationElement notification = $(NotificationElement.class).first();
        Assert.assertEquals("Hello anonymous user", notification.getText());
    }

    @Test
    public void clickButton_showsHelloUserNotificationWhenUserIsNotEmpty() {
        TextFieldElement textField = $(TextFieldElement.class).first();
        textField.setValue("Vaadiner");
        ButtonElement button = $(ButtonElement.class).first();
        button.click();
        Assert.assertTrue($(NotificationElement.class).exists());
        NotificationElement notification = $(NotificationElement.class).first();
        Assert.assertEquals("Hello Vaadiner", notification.getText());
    }

    @Test
    public void pressEnterInTextField_showsHelloUserNotificationWhenUserIsNotEmpty() {
        TextFieldElement textField = $(TextFieldElement.class).first();
        textField.setValue("Vaadiner");
        textField.sendKeys(Keys.ENTER);
        Assert.assertTrue($(NotificationElement.class).exists());
        NotificationElement notification = $(NotificationElement.class).first();
        Assert.assertEquals("Hello Vaadiner", notification.getText());
    }

    @Test
    public void verifyStylesFromAppliedThemeArePresentOnThePage() {
        WebElement verticalLayout = $(VerticalLayoutElement.class).first();
        Assert.assertEquals("centered-content", verticalLayout.getAttribute("class"));
    }

    @Test
    public void verifyComponentStylesAppliedFromTheme() {
        TestBenchElement myField = $(TestBenchElement.class).id(TEXT_FIELD_ID);
        TestBenchElement input = myField.$(DivElement.class)
                .attribute("class", "vaadin-text-field-container").first()
                .$(DivElement.class).attribute("part", "input-field").first();
        Assert.assertEquals("rgba(255, 165, 0, 1)", input.getCssValue("background-color"));
    }

    @Test
    public void verifyApplicationThemeImportCSSWorks() {
        Assert.assertEquals("Imported FontAwesome css file should be applied.",
                "\"Font Awesome 5 Brands\"", $(SpanElement.class).first()
                        .getCssValue("font-family"));

        String iconUnicode = getCssPseudoElementValue(FONT_AWESOME_ID,
                "::before");
        Assert.assertEquals(
                "Font-Icon from FontAwesome css file should be applied.",
                "\"\uf408\"", iconUnicode);
    }

    @Test
    public void staticModuleAsset_servedFromAppTheme() {
        Assert.assertEquals(
                "Node assets should have been copied to 'themes/reusable-theme'",
                getRootURL()
                        + "/themes/gradle-test/fontawesome/svgs/brands/vaadin.svg",
                $(ImageElement.class).first().getAttribute("src"));

        getDriver().get(getRootURL() + "/"
                + $(ImageElement.class).first().getAttribute("src"));
        waitForDevServer();

        Assert.assertFalse("Node static icon should be available",
                driver.getPageSource().contains("HTTP ERROR 404 Not Found"));
    }

    private String getCssPseudoElementValue(String elementId,
                                            String pseudoElement) {
        String script = "return window.getComputedStyle("
                + "document.getElementById(arguments[0])"
                + ", arguments[1]).content";
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return (String) js.executeScript(script, elementId, pseudoElement);
    }
}