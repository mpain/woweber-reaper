package org.mpain.reaper;

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
}
