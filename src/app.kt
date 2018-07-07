import java.io.File

fun main(args: Array<String>) {

    val file = File("./Input")
    if (file.isDirectory){
        file.listFiles().forEach { fileObj ->
            val content = Utils.readFile(fileObj.absolutePath)
            var automata = Utils.createAutomata(content)

            var output = File("./Output/" + fileObj.nameWithoutExtension + ".csv")
            output.writeText(automata.toCSV())

            automata.determinize()
            output = File("./Output/" + fileObj.nameWithoutExtension + "_determinized.csv")
            output.writeText(automata.toCSV())

            automata.removeDeads()
            output = File("./Output/" + fileObj.nameWithoutExtension + "_no_deads.csv")
            output.writeText(automata.toCSV())
        }
    }
}