package homework;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AbstractComponents {

	private WebDriver driver;
	private WebDriverWait wait;

	public AbstractComponents(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		// PageFactory.initElements(driver, this);
	}

	public void waitForElementVisibility(WebElement element) {
		wait.until(ExpectedConditions.visibilityOf(element));
	}

	public void waitForElementStaleness(WebElement element) {
		wait.until(ExpectedConditions.stalenessOf(element));
	}
}