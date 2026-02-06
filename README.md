# Gold Price Monitoring & WhatsApp Notification Service

## Project Overview

This project provides a real-time monitoring service for gold prices and related metals, fetching data from external APIs, calculating Contract for Difference (CFD) prices, and sending timely updates via WhatsApp notifications. The notifications can be sent using either **Twilio WhatsApp API** or the **WhatsApp Cloud API** based on configuration.

The main goals of this project are:

- Fetch up-to-date metal prices (including gold) from a trusted source.
- Convert prices between Kuwaiti Dinar (KWD) and USD using live exchange rates.
- Calculate CFD prices with customizable thresholds to avoid notification spamming.
- Send notifications through WhatsApp with user-configurable integration options.
- Run updates periodically (every 15 seconds) using a scheduled service.

---

## API Calls and Data Sources

### 1. Metal Prices API

- **Endpoint:** `https://api.daralsabaek.com/api/goldAndFundBalance/getMetalPrices`
- **Purpose:** Fetches the latest prices for different metals (gold, silver, etc.) quoted in KWD per gram.
- **Data:** Returns metal type, 24K buy price per gram, price status, and update intervals.

### 2. Currency Exchange Rate API

- **Endpoint:** `https://cdn.moneyconvert.net/api/latest.json`
- **Purpose:** Provides live exchange rates to convert KWD to USD.
- **Data:** JSON object containing currency conversion rates, enabling dynamic USD price calculation.

---

## Project Architecture & Integration

### 1. Data Access Layer (DAO)

- Responsible for calling the external APIs, fetching and parsing metal prices and exchange rates.
- Provides a clean abstraction for services to obtain up-to-date metal prices and USD/KWD conversions.

### 2. Service Layer

- Contains business logic to calculate CFD prices based on fetched data.
- Manages scheduling to poll prices every 15 seconds.
- Applies configurable thresholds to decide when to send notifications.

### 3. Notification Integration Options

This project supports two different methods to send WhatsApp notifications:

#### a) Twilio WhatsApp API

- Uses Twilio’s official Java SDK.
- Requires Twilio account credentials: `Account SID`, `Auth Token`, WhatsApp sender, and receiver numbers.
- Easy to integrate and widely used for programmable messaging.

#### b) WhatsApp Cloud API (Meta)

- Uses direct HTTP REST calls to WhatsApp Cloud API.
- Requires Facebook/Meta app setup with access tokens and phone number ID.
- Offers more native WhatsApp Cloud integration via Meta’s platform.

You can choose the integration method by configuring the appropriate properties in your application settings.

---

## How to Use

1. Configure your API keys and phone numbers in the application properties:

    - For Twilio: set `accountSid`, `authToken`, `whatsappFrom`, and `whatsappTo`.
    - For WhatsApp Cloud API: set `accessToken`, `phoneNumberId`, and `apiVersion`.

2. Run the service to start periodic polling of metal prices.

3. The service calculates CFD prices and sends WhatsApp notifications when the price difference threshold is exceeded.

4. Monitor console logs or configure further logging to track notifications.

---

## Credits & Acknowledgements

- Metal price data courtesy of [Dar Al Sabaek](https://daralsabaek.com/) — thank you for providing a reliable metal pricing API.
- Currency exchange rates provided by [Money Convert](https://moneyconvert.net/company/about/) — a valuable resource for live currency conversions.
- WhatsApp notification integrations powered by:
    - [Twilio WhatsApp API](https://www.twilio.com/whatsapp)
    - [WhatsApp Cloud API by Meta](https://developers.facebook.com/docs/whatsapp/cloud-api)

---
## Disclaimer

This project is provided for **educational and informational purposes only**. The author is **not responsible** for any misuse of the application, including but not limited to:

- Using the service to run or manage financial businesses, investments, or trading decisions.
- Any financial losses, business decisions, or damages caused by reliance on the data provided.
- Any violation of local laws, regulations, or third-party terms.

By using this project, you acknowledge that you use it at your own risk and agree that the author bears no liability.

---
## License

This project is open source and free to use. Please respect the terms of use for all third-party APIs and services.

---

Feel free to contribute, raise issues, or suggest improvements!

---

*Built with 💛 by *Hamad Alhajri**
