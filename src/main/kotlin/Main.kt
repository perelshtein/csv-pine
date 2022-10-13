import java.io.File
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

fun main(args: Array<String>) {
    if(args.size != 2) {
        println("Конвертер .csv в PineScript\n" +
                "Использование: convert входной_файл.csv выходной_файл.txt\n" +
                "Разделитель: точка с запятой\n\n" +
                "(C) 2022 http://github.com/perelshtein\n")
    }

    else {
        val inputName = args[0]
        val outputName = args[1]
        var savedCnt = 0
        var lineCnt = 0
        var dealsLong = mutableListOf<String>()
        var dealsShort = mutableListOf<String>()

        //читаем построчно входной файл, кроме заголовка
        try {
            var i = 0

            File(inputName).forEachLine {
                if(i > 0) {
                    var deal = mutableListOf<String>()
                    it.split(";").forEach { deal.add(it.trim(';')) }

                    if(deal.size < 2) {
                        println("Пропуск строки номер ${lineCnt + 1}: указано менее 3х значений")
                    }
                    //сохранено более 1000 сделок?
                    else if(savedCnt >= 1000) {
                        throw TooManyDeals("")
                    }
                    else {
                        //есть сигнал на покупку?
                        if(!deal[1].toDouble().isNaN()) {
                            val dateParsed = ZonedDateTime.parse(deal[0])
                            dealsLong.add("dLong := t == timestamp(${dateParsed.year}, ${dateParsed.monthValue}, ${dateParsed.dayOfMonth}, " +
                                    "${dateParsed.hour}, 0, 0) ? low - step : dLong")
                            savedCnt++
                        }
                        //или есть сигнал на продажу?
                        else if(!deal[2].toDouble().isNaN()) {
                            val dateParsed = ZonedDateTime.parse(deal[0])
                            dealsShort.add("dShort := t == timestamp(${dateParsed.year}, ${dateParsed.monthValue}, ${dateParsed.dayOfMonth}, " +
                                    "${dateParsed.hour}, 0, 0) ? high + step : dShort")
                            savedCnt++
                        }
                    }
                }
                i++
                lineCnt++
            }
        }

        catch(e: IOException) {
            print("Ошибка чтения файла $inputName")
            return
        }

        catch(e: DateTimeParseException) {
            //уже были сделки? тогда сохраняем их в файл
            if(savedCnt > 0 && saveFile(outputName, dealsLong, dealsShort)) {
                println("Сохранено: ${savedCnt} сделок.\n" +
                        "Прекращаю работу: ошибка разбора даты, строка номер ${lineCnt + 1}")
            }
            else println("Прекращаю работу: ошибка разбора даты, строка номер ${lineCnt + 1}")
            return
        }

        catch(e: NumberFormatException) {
            //уже были сделки? тогда сохраняем их в файл
            if(savedCnt > 0 && saveFile(outputName, dealsLong, dealsShort)) {
                println("Сохранено: ${savedCnt} сделок.\n" +
                        "Прекращаю работу: ошибка разбора столбца Buy/Sell, строка номер ${lineCnt + 1}")
            }
            else println("Прекращаю работу: ошибка разбора столбца Buy/Sell, строка номер ${lineCnt + 1}")
            return
        }

        catch(e: TooManyDeals) {
            if(saveFile(outputName, dealsLong, dealsShort)) println("Прекращаю работу: сохранено 1000 сделок.\n" +
                    "Pine больше не поддерживает.")
            else println("Прекращаю работу: найдено более 1000 сделок.\n" +
                    "Pine больше не поддерживает")
            return
        }

        if(saveFile(outputName, dealsLong, dealsShort)) println("Все отлично!\n" +
                "Сохранено сделок: ${savedCnt}\n" +
                "Long: ${dealsLong.size}\n" +
                "Short: ${dealsShort.size}")
    }
}

class TooManyDeals(message: String) : Exception(message)

//записываем выходной файл
fun saveFile(outputName: String, dealsLong: List<String>, dealsShort: List<String>) : Boolean {
    try {
        var out = File(outputName).bufferedWriter()
        dealsLong.forEach { out.write("${it}\n") }
        out.write("\n")
        dealsShort.forEach { out.write("${it}\n") }
        out.close()
    }
    catch(e: IOException) {
        println("Ошибка записи файла $outputName")
        return false
    }
    return true
}
