# mobile-web-best-practice-container

为 [mobile-web-best-practice](https://github.com/mcuking/mobile-web-best-practice) 提供演示相关功能的安卓项目。

目前有如下功能：

1. 集成 [DSBridge-Android](https://github.com/wendux/DSBridge-Android)

2. 向 h5 提供同步到本地日历功能，API 如下：

```ts
interface SyncCalendarParams {
  id: string; // 日程唯一标识符
  title: string; // 日程名称
  location: string; // 日程地址
  startTime: number; // 日程开始时间
  endTime: number; // 日程结束时间
  alarm: number[]; // 提前提醒时间，单位分钟
}

dsbridge.call('syncCalendar', params: SyncCalendarParams, cb);
```
