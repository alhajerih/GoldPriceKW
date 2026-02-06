package com.alhajri.goldPrice;

import com.alhajri.goldPrice.config.WhatsAppService;
import com.alhajri.goldPrice.entity.MetalCfdResult;
import com.alhajri.goldPrice.DAO.MetalPriceDaoImpl;
import com.alhajri.goldPrice.services.LiveMetalPriceService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GoldPriceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoldPriceApplication.class, args);


		WhatsAppService whatsAppService = new WhatsAppService();
		MetalPriceDaoImpl dao = new MetalPriceDaoImpl();
		LiveMetalPriceService liveService = new LiveMetalPriceService(dao,whatsAppService);

		// Start 15s polling
		liveService.start();

		// Run for 1 minute and print live prices every 15 seconds
		for (int i = 0; i < 4; i++) {
			MetalCfdResult prices = liveService.getLatestPrices().getFirst();
			if (prices != null) {
				System.out.println(
						"MetalType=" + prices.getMetalType() +
								", KWD/gram=" + prices.getBuyPrice24KWD() +
								", CFD USD/oz=" + prices.getCfdPriceUSD()
				);
			}
			System.out.println("-----");
		}
	}

}
