package homework;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class SeleniumPage extends AbstractComponents{

	WebDriver driver;

	public SeleniumPage(WebDriver driver) {
        super(driver);
		this.driver = driver;
		PageFactory.initElements(driver, this);

	}

	// PageFactory

	@FindBy(xpath = "//summary[@class='Button--primary Button--medium Button flex-1 d-inline-flex']")
	WebElement codeButton;

	@FindBy(css = "clipboard-copy[value*='selenium.git']")
	WebElement cloneURL;

	@FindBy(xpath = "//span[@title='83']")
	WebElement releasesNr;

	public void openDialogCode() throws InterruptedException {

		codeButton.click();
		Thread.sleep(2000);
		//waitForElementVisibility(codeButton);
	}

	public String getRepoURL() throws InterruptedException {
		
		cloneURL.click();
		Thread.sleep(2000);
		return getClipboardText();
	}

	public String getNrReleases() throws InterruptedException {

		return releasesNr.getText();

	}

	public String getClipboardText() {
		try {
			return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
