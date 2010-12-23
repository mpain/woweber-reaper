package org.mpain.reaper;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.mpain.reaper.http.HttpExchangeException;
import org.mpain.reaper.http.IHttpExchange;
import org.mpain.reaper.service.IReaperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

@ContextConfiguration(locations = { "classpath:/application-context.xml" })
public class ReaperServiceTest extends AbstractTestNGSpringContextTests {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private IReaperService reaper;

	@Autowired
	private IHttpExchange exchange;

	@Test
	public void getPageCountTest() throws HttpExchangeException {
		try {
			exchange.executeGet("http://woweber.com/page.php?book=cos&page=1");
			if (exchange.getExchangeData().getCode() == 200) {
				reaper.open(exchange.getExchangeData().getResponse());

				int count = reaper.getPagesCount();
				Assert.assertEquals(count, 87);

				log.info(reaper.getContent());
			}

		} finally {
			exchange.close();
			reaper.close();
		}
	}

	@Test
	public void xsltTest() {
		try {
			File xsltFile = new File("./target/classes/simple.xslt");
			URL url = new URL("http://woweber.com/page.php?book=cos&page=1");
			
			final Tidy tidy = new Tidy();
			tidy.setQuiet(true);
			tidy.setShowWarnings(false);
			tidy.setForceOutput(true);
			tidy.setUpperCaseTags(false);
			Document document = tidy.parseDOM(new InputStreamReader(url.openStream(), "windows-1251"), null);
			
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			StreamSource styleSource = new StreamSource(xsltFile);

			Transformer transformer = transformerFactory.newTransformer(styleSource);
			
			transformer.setParameter("startToc", "toc");
			transformer.setParameter("tocIndex", "123");
			
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(System.out);
			transformer.transform(source, result);
		} catch (Exception e) {
			log.error("ERROR!!!", e);
		}
	}
}
