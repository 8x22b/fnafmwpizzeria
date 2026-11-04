# FNAFMW Pizzeria Dashboard

Фанатский аддон для **FNAF: Management Wanted** на Minecraft **1.21.1 / NeoForge**.

Возвращает старые пиццерии и постройки из 0.3.3, добавляет натуральную генерацию, магазин в терминале, deed'ы и лут.

> Неофициальный фанатский проект. Не связан с OVDR Studios, Scott Cawthon и FNAF. Подробности — в `NOTICE.md`.

## Что есть

- 5 пиццерий из старой версии: Discount Diner, Fredbear's, Jeff's Pizza, FNAF 1, FNAF 2.
- Натуральная генерация в новых чанках: 5 пиццерий, будка FNAF 4 с терминалом, большой шатёр и карнавал.
- Deed'ы в креативной вкладке и в магазине терминала.
- Магазин: 5 deed'ов и 40+ блоков FNAFMW, английские и русские названия.
- Лут: сундуки/бочки с loot table, шкафчики с fazcoin/fazbuck, паутиной, string, emerald, diamond, name tag и редкими масками.
- Экономика и посетители для заявленных пиццерий.
- Команды `/pizzeria`, `quality`, `rescan`, `claim`, `spawn`, `cleanup`, `place <deed>`, `loot`.

## Требования

- Minecraft 1.21.1
- NeoForge 21.1.235+ (проверено на 21.1.235 и 21.1.265)
- FNAF: Management Wanted `1.0.0-snapshot-1`
- GeckoLib 4.9.2, Veil 4.5.2 (нужны FNAFMW)
- Java 21

Refraction и Vixis уже лежат внутри FNAFMW. Старый jar FNAFMW для 1.20.1 ставить рядом нельзя.

## Установка

1. Установи Minecraft 1.21.1 + NeoForge 21.1.235+.
2. Положи FNAFMW, GeckoLib, Veil и `fnafmwpizzeria-1.1.0.jar` в `mods`.
3. Запусти игру.

Готовый jar: [`dist/fnafmwpizzeria-1.1.0.jar`](dist/fnafmwpizzeria-1.1.0.jar).

## Генерация и поиск

Структуры появляются **только в новых чанках**. Старые миры и уже сгенерированные чанки не обновляются.

```mcfunction
/locate structure fnafmwpizzeria:pizzerias
/locate structure fnafmwpizzeria:worldgen_extras
```

Или возьми deed в креативе / купи в терминале.

## Лут

- Сундуки и бочки используют loot table `fnafmwpizzeria:chests/loot`.
- Шкафчики заполнены заготовленным лутом.
- `/pizzeria loot` (op) заполняет пустые локеры/сундуки/бочки **в уже загруженных чанках** вокруг игрока. Чанки не грузит и не генерирует.
- Команда заполняет все пустые контейнеры в радиусе 40, включая твои собственные пустые сундуки. Лучше стоять внутри структуры.

## Сборка

FNAFMW не лежит на публичном Maven, поэтому его jar'ы не входят в репозиторий.

1. Положи в `libs/`:
   - `fnafmw-alpha-1.0.0-snapshot-1.jar`
   - `refraction-neoforge-1.21.1-1.3.0-mw1.jar`
   - `vixis-0.7.0.jar`
   - `geckolib-neoforge-1.21.1-4.9.2.jar`
   - `veil-neoforge-1.21.1-4.5.2.jar`
2. Выполни `./gradlew build` (на Windows — `gradlew.bat build`) или открой проект в IntelliJ IDEA.

Готовый jar будет в `build/libs/`. Если Gradle не находит версию ModDevGradle из `build.gradle`, поставь версию из актуального NeoForge MDK.

## Ограничения

- Натуральные пиццерии не приносят доход, пока их чанки не заявлены; deed'ы заявляют чанки сами.
- Старые чанки, уже поставленные пиццерии и шатры не обновляются.
- Биом Fazbear Hills пока не восстановлен: в новом моде остались блоки и дерево, но старая инъекция биома удалена.
- Клиентский визуал и UI проверяются вручную.

## Благодарности

- **OVDR Studios** — FNAF: Management Wanted и старые структуры/ассеты.
- **Fork** — код аддона, конвертация, worldgen, магазин и лут.
- **Scott Cawthon** — Five Nights at Freddy's.

## Лицензия

Код аддона — MIT (`LICENSE`). Структуры, текстуры и другие ассеты, производные от FNAFMW/FNAF, принадлежат их правообладателям (`NOTICE.md`).
