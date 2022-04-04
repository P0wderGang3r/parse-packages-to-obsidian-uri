import java.io.File
import java.io.FileReader
import java.io.FileWriter

val fileRegistry: ArrayList<String> = ArrayList()


fun writeText(inputString: String, fileWriter: FileWriter): Boolean {
    if (inputString != "") {
        val newString = inputString.split(": ")[1]
        fileWriter.write("$newString\n")

        return true
    }

    return false
}

fun dependencyToURI(inputString: String, fileWriter: FileWriter): Boolean {
    val dependenciesList = inputString.split(" , ", ", ", ",")

    for (dependency in dependenciesList) {
        if (!dependency.equals(dependenciesList[dependenciesList.size - 1]))
            fileWriter.write("[[$dependency | $dependency]], ")
        else
            fileWriter.write("[[$dependency | $dependency]]\n")
    }

    return true
}

fun removeDependencyVersions(inputString: String): String {
    var dependenciesString = ""
    var currIndex = 0

    while (currIndex in inputString.indices) {
        if (inputString[currIndex] == '(') {
            while (inputString[currIndex] != ')') {
                currIndex++
            }
        }

        if (inputString[currIndex] != ')')
            dependenciesString = dependenciesString.plus(inputString[currIndex])
        currIndex++
    }

    return dependenciesString
}

fun dependenciesProcession(inputString: String, fileWriter: FileWriter): Boolean {
    if (inputString != "" && inputString.split(": ")[0].equals("Depends")
        || inputString != "" && inputString.split(": ")[0].equals("Recommends")
        || inputString != "" && inputString.split(": ")[0].equals("Suggests")) {
        val dependenciesString = removeDependencyVersions(inputString.split(": ")[1])
        dependencyToURI(dependenciesString, fileWriter)
        return true
    }

    return false
}

//Причина комментирования строки - слишком сильное влияние оказывает "приоритет" ПО.
//Это происходит ввиду того, что практически все приложения в репе опциональны.
fun writeTag(inputString: String, fileWriter: FileWriter): Boolean {
    if (inputString != "" && inputString.split(": ")[0].equals("Section")
        /*|| inputString != "" && inputString.split(": ")[0].equals("Priority") */) {
        val newString = inputString.split(": ")[1]
            fileWriter.write("#$newString\n")
        return true
    }

    return false
}

fun descriptionFix(inputString: List<String>, curr_index: Int, fileWriter: FileWriter): Int {
    var lIndex = curr_index
    if (inputString[lIndex].split(": ")[0] == "Description") {
        fileWriter.write("### " + inputString[lIndex].split(": ")[1] + "\n")
        lIndex++
        while (lIndex < inputString.size && inputString[lIndex] != "") {
            fileWriter.write(inputString[lIndex] + "\n")
            lIndex++
        }
    }

    return lIndex
}

fun writeCategory(inputString: String, fileWriter: FileWriter): Boolean {
    if (inputString != "") {
        val newString = inputString.split(": ")[0]
        if (inputString.split(": ")[0].equals("Package")) {
            fileWriter.write("# $newString\n")
        } else
            fileWriter.write("## $newString\n")

        return true
    }

    return false
}

fun createFiles(input_path: String, output_path: String): Boolean {
    //Проверка наличия директории для записи
    var file = File(output_path)

    if (!file.exists()) {
        file.mkdir()
    }

    //Проверка наличия входного файла
    file = File(input_path)
    if (!file.exists())
        return false

    val inputString = FileReader(file).readLines()

    var fileWriter: FileWriter
    var currIndex: Int
    var packageName: String
    var fixIndex: Int

    for (line in inputString.indices) {
        if (inputString[line].split(": ")[0].equals("Package")) {
            currIndex = line
            packageName = inputString[line].split(": ")[1]

            fileRegistry.add("$packageName.md")

            fileWriter = FileWriter("$output_path/$packageName.md")
            fileWriter.flush()

            do {
                writeCategory(inputString[currIndex], fileWriter)

                fixIndex = descriptionFix(inputString, currIndex, fileWriter)
                if (fixIndex != currIndex) {
                    currIndex = fixIndex
                    continue
                }

                if (writeTag(inputString[currIndex], fileWriter)) {
                    currIndex++
                    continue
                }

                if (dependenciesProcession(inputString[currIndex], fileWriter)) {

                    currIndex++
                    continue
                }

                writeText(inputString[currIndex], fileWriter)
                currIndex++

            } while (currIndex < inputString.size && inputString[currIndex].split(": ")[0] != "Package")

            fileWriter.close()
        }
    }

    return true
}

/**
 * Входные параметры:
 * args[0] - путь до файла, в котором лежит информация о пакетах в репе.
 * args[1] - папка, в которую будет осуществляться вывод содержимого.
 * Здесь нужна бы проверка условий, но так как для себя, то можно:)
 */
fun main(args: Array<String>) {
    println("Program arguments: ${args.joinToString()}")

    val inputPath = args[0]
    val outputPath = args[1]

    createFiles(inputPath, outputPath)
}