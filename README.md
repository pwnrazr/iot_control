# IoT Control App
An app to control my IoT stuff

Communication is done through HTTP requests

To Do:
- Something to control brightness of Ambient LED
- ~~Settings page to set IP address of MCUs (currently hardcoded)~~ https://github.com/pwnrazr/iot_control_app/commit/647fc3e497204f078ddece615a79a5f46fee1fcd
- ~~Solve a bug where it would spam on/off nonstop until app is killed (might be microcontroller side?)~~
https://github.com/pwnrazr/ESP32_Ambient_Lighting/commit/c44158fc03d606fc75912ae00c070b030b8bf715
https://github.com/pwnrazr/ESP8266_Room-Lights-Control/commit/a3f4c5e6cd2501ea6a343855f4e0ee42b6cfb4de
- Implement RGB control for ambient LED
- Move IP address settings to its own settings page

For microcontroller side:
- https://github.com/pwnrazr/ESP32_Ambient_Lighting
- https://github.com/pwnrazr/ESP8266_Room-Lights-Control
