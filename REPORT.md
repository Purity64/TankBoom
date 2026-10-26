# รายงานตรวจสอบโปรเจค TankBoom

- วันที่ตรวจ: 2026-10-26
- ขอบเขต: `TankBoom` (client) และ `serverTankBoom` (server)
- คอมไพเลอร์: javac 17.0.20.1

## ผลการคอมไพล์

**ผ่านทั้งสองโปรเจค ไม่มี compile error** (build ลง temp เท่านั้น)

ก่อนหน้านี้ที่ import `Game` ไม่ได้ เกิดจาก `Game` อยู่ default package ซึ่ง named package import ไม่ได้ — ตอนนี้ย้ายเป็น `package Game;` + `import Game.Game;` แล้ว จึงคอมไพล์ผ่าน

> ข้อแนะนำเชิงโครงสร้าง: ชื่อ package ควรเป็นตัวพิมพ์เล็ก (`package game;`) ตาม convention ของ Java

---

## P0 — ทำให้เล่นไม่ได้ / หลุดทันที

| # | จุดเกิด | อาการที่เห็น | สาเหตุ | ต้องแก้ |
|---|---------|--------------|--------|---------|
| 1 | `Game/Game.java:107` | tank โผล่มา 1 คันแล้วค้าง, console ขึ้น `[ERROR] Connection Error` แล้ว client ตัดการเชื่อมต่อ | `payerArr.get(i)` เรียก index 1 บน list ว่าง → `IndexOutOfBoundsException` หลุดออกไปถึง catch ใน `Server_cilent` → `disconnect()` | เปลี่ยนเป็นตรวจ hwid ที่มีอยู่แทน (`findTank(hwid) != null`) ไม่ใช้ `get(i)` |
| 2 | `Game/Game.java:56` | คลิกแล้วไม่มีการยิง | `//t1.shoot(...)` ถูกคอมเมนต์ และไม่มี field เก็บ tank ของตัวเอง (`t1` เป็น local) | เพิ่ม field `tank myTank` ตั้งค่าเมื่อ hwid ตรงกับ `Server_cilent.getHwid()` แล้วผูก `myTank.shoot(...)` |
| 3 | `config/Server_cilent.java:17` | เปิด client ตัวที่ 2 แล้วไม่เกิดอะไร / หลุดเงียบ ๆ | `hwid = "jeera"` ตายตัว → `Server.isCheckHwid()` ปฏิเสธและปิด socket | ตั้ง hwid ไม่ซ้ำต่อเครื่อง (args/file/random) ก่อน `connect()` และเพิ่ม `getHwid()` |
| 4 | `serverTankBoom/Server.java:39-40` + `ClientHandler.java:30-31` | client ใหม่บางครั้งไม่ได้รับ player list เลย (คนแรกจะว่างจนกว่าจะมีคนที่ 2 เข้า) | `handler.start()` แล้วเรียก `getHwid()` ทันที แต่ `ClientHandler.out` ยังเป็น `null` → `sendMessage` ข้ามไป | สร้าง `out/in` ใน constructor หรือส่ง list หลัง handler พร้อมแล้ว |
| 5 | `Game/Game.java` (ทั้งไฟล์) | ถ้าไม่เปิด server หน้าต่างจะว่าง ไม่มี tank ให้ยิง | ตัวผู้เล่นถูกสร้างเฉพาะเมื่อได้ข้อความ `player:` จาก server | สร้าง player ของตัวเองในเครื่องก่อน แล้วค่อย merge รายชื่อจาก server |

---

## P1 — เล่นได้แต่พฤติกรรมผิด / ค้าง

| # | จุดเกิด | อาการ | สาเหตุ | ต้องแก้ |
|---|---------|-------|--------|---------|
| 6 | `Game/Game.java:100-116` | tank ผีเพิ่มขึ้นเรื่อย ๆ ทุกครั้งที่มีคนเชื่อมต่อ, ตำแหน่งทับกัน | `CreatePlayer` สร้าง tank ใหม่ทุกคนใน list ทุกครั้งที่ broadcast และ `payerArr.get(i) != t1` เป็นจริงเสมอเพราะ `t1` เพิ่ง `new` | ทำ reconcile: ถ้า hwid มีอยู่แล้วข้าม, ถ้า hwid หลุดให้ลบ tank, กำหนดตำแหน่งเกิดไม่ให้ทับ |
| 7 | `serverTankBoom/Server.java:75-77` | คนที่ปิดเกมยังค้างอยู่ในรายชื่อของคนอื่น | `removeClient()` ไม่ broadcast list ใหม่ | หลัง remove ให้เรียก `getHwid()` |
| 8 | `items/tank.java:158-162` + `config/Server_cilent.java:42-53` | เห็นผู้เล่นอื่นอยู่นิ่ง | ส่ง `hwid:move:x,y` ไป server แล้ว แต่ client ไม่ parse `move` (รับแค่ `player`) | เพิ่ม parser `move` แล้วเรียก `findTank(hwid).setLocation(x,y)` |
| 9 | `items/tank.java:173-199` | กระสุน/ดาเมจไม่ sync ระหว่างผู้เล่น | ยิงและลด HP แบบ local only | ส่ง `shoot`/`damage` ผ่าน server (งานออกแบบ protocol) |
| 10 | `items/tank.java:144-154`, `117-123` | หลอดเลือดไม่หด/ไม่เพิ่มจนกว่ารถจะขยับ | เปลี่ยน `hp` แต่ไม่เรียก `repaint()` | เติม `repaint()` หลังเปลี่ยน HP |
| 11 | `items/tank.java:52`, `125-139` | Tank2–Tank6 เก็บ shield แล้วตัวล่องหน; shield ไม่หมดตามเวลาเป๊ะ | `Tank2_shield.png`…ไม่มีใน `ImageConfig` → `shieldImage == null` → `setBackImage(null)` ทำให้ไม่วาดอะไร; Timer ถูก `return` แต่ไม่ `cancel()` และถ้าเก็บซ้ำจะมีหลาย Timer | ตั้ง image สำรองเมื่อ null, `timer.cancel()` ตอนหมดเวลา, หยุด Timer เดิมก่อนเริ่มใหม่ |
| 12 | `items/bullet/ball.java:21`, `ballBoom.java:21` | ยิงขึ้น/ลงแล้วกระสุนค้างบนจอ + thread วนตลอดไป | ตรวจขอบจอเฉพาะ `main_x` | เพิ่มเงื่อนไข `main_y <= 0 \|\| main_y >= mFrame.getHeight()` |
| 13 | `items/bullet/ball.java:27-48`, `ballBoom.java:27-48` | กระสุนทะลุกำแพงไปโดน tank ในเฟรมเดียวกัน | `break` ออกจาก for ของกำแพง แต่ยังไปต่อที่ลูป tank + เคลื่อนที่อีก 1 เฟรม | ใช้ `return` หลัง `kill()` |
| 14 | `items/tank.java:39` | HP เริ่มที่ 50/100 | `hp = GameConfig.TankHP - 50` (ค่าคงเหลือจากการเทสต์) | เปลี่ยนเป็น `GameConfig.TankHP` |
| 15 | `items/tank.java:163-169` | ผู้เล่นที่ HP 0 ยังค้างในหน้าจอคนอื่น | `kill()` ลบเฉพาะเครื่องตัวเอง | แจ้ง server แล้ว broadcast สถานะตาย/ลบออกจาก list |

---

## P2 — คุณภาพโค้ด / เสถียรภาพ

| # | จุดเกิด | เรื่อง | ต้องแก้ |
|---|---------|--------|---------|
| 16 | `config/Server_cilent.java:49` → `Game.CreatePlayer` | แก้ Swing จาก network thread | ครอบด้วย `SwingUtilities.invokeLater(...)` |
| 17 | `serverTankBoom/ClientHandler.java:30` | สร้าง `BufferedReader` ตัวใหม่บน socket ที่ `Server` อ่าน hwid ไปแล้ว อาจทำข้อมูลที่ buffer ไว้หาย | ส่ง `bufferIn` จาก `Server` เข้า constructor |
| 18 | `Game/Game.java:101,105` | `System.out.println` debug ค้าง | ลบหรือปิดด้วย flag |
| 19 | `config/Server_cilent.java:54-58` | ต่อ server ไม่ได้แล้วไม่ retry | เพิ่ม loop retry หรือปุ่ม reconnect |
| 20 | `items/bullet/interface_bullet.java:43` | `new Thread(this).start()` ใน constructor (this-escape) | ย้ายไปเริ่ม thread หลังสร้าง object เสร็จ |
| 21 | `serverTankBoom/ClientHandler.java:38` | catch `IOException` เงียบ ไม่มี log | log อย่างน้อย 1 บรรทัด |
| 22 | `items/tank.java:159` | ตอนออฟไลน์ `Move()` เตือนทุก 5px → console spam | เช็ค `Server_cilent.isConnected()` ก่อนส่ง |

---

## ลำดับที่แนะนำให้แก้

1. **P0 #1** — ให้ `CreatePlayer` ไม่ throw และไม่สร้างซ้ำ (ปลดบล็อกการเชื่อมต่อ)
2. **P0 #3** — hwid ไม่ซ้ำ เพื่อทดสอบ 2 client บนเครื่องเดียว
3. **P0 #4** — ให้ client ใหม่ได้รับ player list แน่นอน
4. **P0 #2** — เก็บ `myTank` + เปิดการยิง
5. **P1 #8, #9** — sync การเคลื่อนที่และกระสุน
6. **P1 #6, #7, #11, #12, #13, #14** — ความถูกต้องของเกม
7. P2 เก็บกวาด

---

## วิธีทดสอบ

```bash
# 1) server
cd "C:/Users/purity/Desktop/java projuct/serverTankBoom"
javac -encoding UTF-8 -d . *.java
java Server

# 2) client (เปิด 2 หน้าต่าง ต้องตั้ง hwid ต่างกันก่อน)
cd "C:/Users/purity/Desktop/java projuct/TankBoom"
find . -name "*.java" -print0 | xargs -0 javac -encoding UTF-8 -d .
java Game.Game
```

เกณฑ์ผ่าน:
- แต่ละ client เห็น tank ของตัวเอง 1 คัน และเห็นอีกฝั่ง
- คลิกแล้วยิงได้ กระสุนลด HP ฝั่งที่โดน
- ขยับแล้วอีกฝั่งเห็นตำแหน่งเปลี่ยน
- ปิด client หนึ่งแล้วรายชื่อฝั่งที่เหลืออัปเดต
- ไม่มี exception ใน console
