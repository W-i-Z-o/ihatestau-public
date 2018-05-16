package de.tinf15b4.ihatestau.kamerascraper.downloader;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class CameraDownloader {

	private String getUrl(String cameraname) {
		return "https://www.svz-bw.de/kamera/ftpdata/" + cameraname + "/" + cameraname + "_gross.jpg?"
				+ DownloadUtil.generateJsTimestamp();
	}

	private String getReferer(String cameraname) {
		return "https://www.svz-bw.de/fileadmin/templates/vizbw1/kameradetail.php?id=" + cameraname;
	}

	private String getUserAgent() {
		return "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)";
	}

	public byte[] getImage(String cameraname) throws HttpException, IOException {

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpGet req = new HttpGet(this.getUrl(cameraname));

			String ua = this.getUserAgent();
			String referer = this.getReferer(cameraname);

			req.setHeader("User-Agent", ua);
			req.setHeader("Referer", referer);

			RequestConfig c = RequestConfig.custom()
					.setConnectionRequestTimeout(400)
					.setConnectTimeout(400)
					.setSocketTimeout(400).build();
			req.setConfig(c);

			try (CloseableHttpResponse res = httpClient.execute(req); InputStream is = res.getEntity().getContent()) {

				if (res.getStatusLine().getStatusCode() != 200) {
					throw new HttpException("Error retrieving " + req.getURI() + ": " + res.getStatusLine().toString());
				}

				return IOUtils.toByteArray(is);
			}
		}
	}

}
