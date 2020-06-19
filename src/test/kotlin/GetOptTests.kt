import com.hankadler.io.GetOpt

fun main() {
    var argv: Array<String>
    var shortOpts: String
    var longOpts: List<String>

    shortOpts = "rfi:"
    testParseShortOpts(shortOpts)

    // Good
    longOpts = listOf("long", "long-opt=", "ll")
    testParseLongOpts(longOpts)

    // Bad
    longOpts = listOf("long ", "0long", "a", "long-", "--", "long-option-=")
    for (opt in longOpts) {
        testParseLongOpts(listOf(opt))
    }

    // Case 01
    shortOpts = "hrfi:Hw:"
    longOpts = listOf("help", "recursive", "force", "input=", "human-readable", "with-value=")
    argv = arrayOf("-r", "--force", "-ipepe.txt", "papo", "-H", "-w", "some list", "arg1", "arg2")
    testGetOpt(argv, shortOpts, longOpts)

    // Case 02
    shortOpts = "h:ri:"
    longOpts = listOf("help", "recursive", "input=", "with-value=")
    argv = arrayOf("-r", "-ipepe.txt", "papo", "arg1", "arg2")
    testGetOpt(argv, shortOpts, longOpts)

    // Case 03
    shortOpts = "hi:"
    longOpts = listOf("help", "input=", "with-value=")
    arrayOf("-r", "-ipepe.txt", "papo", "arg1", "arg2")
    testGetOpt(argv, shortOpts, longOpts)
}

fun testGetOpt(argv: Array<String>, shortOpts: String, longOpts: List<String>) {
    println("\n--- Test: getOpt() ---")
    println("INPUT")
    println("         argv: ${argv.toList()}")
    println("    shortOpts: $shortOpts")
    println("     longOpts: $longOpts")

    val (opts, args) = GetOpt.getOpt(argv, shortOpts, longOpts)

    println("OUTPUT")
    println("    opts: $opts")
    println("    args: ${args.toList()}")
}

fun testParseShortOpts(shortOpts: String) {
    println("\n--- Test: parseShortOpts() ---")
    println("INPUT")
    println("    shortOpts: $shortOpts")
    println("OUTPUT")
    println("    " + GetOpt.parseShortOpts(shortOpts))
}

fun testParseLongOpts(longOpts: List<String>): Boolean {
    println("\n--- Test: areLongOptsValid() ---")
    println("INPUT")
    println("    longOpts: $longOpts")
    println("OUTPUT")
    return try {
        println("    " + GetOpt.parseLongOpts(longOpts))
        true
    } catch (e: Exception) {
        println("    " + e.message)
        false
    }
}
