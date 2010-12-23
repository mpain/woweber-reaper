package org.mpain.reaper.service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReaperService implements IReaperService {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private static final Pattern DELIMITER_PATTERN = Pattern.compile("^\\s?(?:\\*\\s*)+$");
	
	private ThreadLocal<Document> document = new ThreadLocal<Document>();
	
	@Override
	public void open(String source) {
		Document parsed = Jsoup.parse(source);
		document.set(parsed);
	}
	
	@Override
	public void close() {
		document.remove();
	}
	
	@Override
	public int getPagesCount() {
		checkDocument();
		
		Element element = document.get().select("table.navread tr td a").last();
		return (element != null) ? parseInt(element.text()) : -1;
	}

	private void checkDocument() {
		if (document.get() == null) {
			throw new IllegalStateException("Document isn't set");
		}
	}
	
	@Override
	public String getContent() {
		checkDocument();
		Map<String, String> tocMap = new HashMap<String, String>();
		
		StringBuilder builder = new StringBuilder();
		int index = 1;
		Elements elements = document.get().select("body p");
		for (Element element : elements) {
			String className = element.attr("class");
			
			boolean skip = false;
			if (className.equals("r_head")) {
				skip = DELIMITER_PATTERN.matcher(element.text()).matches();
				if (!skip) {
					String toc = "toc-" + index;
					element.attr("id", toc);
					
					tocMap.put(toc, element.text());
					index++;
				}
			}
			
			if (skip || className.equals("") || className.equals("r_top")) {
				continue;
			}
			
			
			builder.append(element.toString());
		}
		
		log.debug("TOC: {}", tocMap);
		
		return builder.toString();
	}
	
	private int parseInt(String source) {
		try {
			return Integer.parseInt(source);
		} catch (NumberFormatException e) {
		}
		
		return -1;
	}
}
