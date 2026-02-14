# Gold Price Monitoring & WhatsApp/Telegram Notification Service

## Project Overview

This project provides a real-time monitoring service for gold prices and related metals, fetching data from external APIs, calculating Contract for Difference (CFD) prices, and sending timely updates via **WhatsApp** or **Telegram** notifications. The notifications can be sent using either **Twilio WhatsApp API**, **WhatsApp Cloud API**, or **Telegram Bot API** based on configuration.

The main goals of this project are:

- Fetch up-to-date metal prices (including gold) from a trusted source.
- Convert prices between Kuwaiti Dinar (KWD) and USD using live exchange rates.
- Calculate CFD prices with customizable thresholds to avoid notification spamming.
- Send notifications through WhatsApp or Telegram with user-configurable integration options.
- Run updates periodically (every 15 seconds) using a scheduled service.
- Support Arabic language for Telegram messages with KWD currency.

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

This project supports three different methods to send notifications:

#### a) Twilio WhatsApp API

- Uses Twilio's official Java SDK.
- Requires Twilio account credentials: `Account SID`, `Auth Token`, WhatsApp sender, and receiver numbers.
- Easy to integrate and widely used for programmable messaging.

#### b) WhatsApp Cloud API (Meta)

- Uses direct HTTP REST calls to WhatsApp Cloud API.
- Requires Facebook/Meta app setup with access tokens and phone number ID.
- Offers more native WhatsApp Cloud integration via Meta's platform.

#### c) Telegram Bot API

- Uses direct HTTP REST calls to Telegram Bot API.
- Requires Telegram Bot token and chat ID.
- Supports Arabic language messages with KWD pricing information.
- Example command: `sell 5 grems` returns price in Arabic with KD currency.

You can choose the integration method by configuring the appropriate properties in your application settings.

---

## How to Use

1. Configure your API keys and settings in the application properties:

    - For Twilio: set `accountSid`, `authToken`, `whatsappFrom`, and `whatsappTo`.
    - For WhatsApp Cloud API: set `accessToken`, `phoneNumberId`, and `apiVersion`.
    - For Telegram: set `telegramBotToken` and `telegramChatId`.

2. Run the service to start periodic polling of metal prices.

3. The service calculates CFD prices and sends notifications when the price difference threshold is exceeded.

4. For Telegram, users can input commands like `sell 5 grems` to receive prices in Arabic with KD currency.

5. Monitor console logs or configure further logging to track notifications.

---

## Credits & Acknowledgements

- Metal price data courtesy of <a href="https://daralsabaek.com/" target="_blank">Dar Al Sabaek</a> thank you for providing a reliable metal pricing API.
- Currency exchange rates provided by <a href="https://moneyconvert.net/company/about/" target="_blank">Money Convert</a> a valuable resource for live currency conversions.
- Notification integrations powered by:
    - <a href="https://www.twilio.com/whatsapp" target="_blank">Twilio WhatsApp API</a>
    - <a href="https://developers.facebook.com/docs/whatsapp/cloud-api" target="_blank">WhatsApp Cloud API by Meta</a>
    - <a href="https://core.telegram.org/bots/api" target="_blank">Telegram Bot API</a>

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

*Built with 💛 by <a href="www.hamadalhajeri.com" target="_blank">Hamad Alhajeri</a>*
