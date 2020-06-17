import com.hankadler.io.GetOpt

fun main() {
    println("\n--- Test screenShortOpts() ---")
    println(GetOpt.screenShortOpts("h"))
    println(GetOpt.screenShortOpts("rf"))
    println(GetOpt.screenShortOpts("rfi:"))
    println(GetOpt.screenShortOpts(":"))

    println("\n--- Test screenLongOpts() ---")
    println(GetOpt.screenLongOpts(listOf("help")))
    println(GetOpt.screenLongOpts(listOf("recursive", "long-option")))
    println(GetOpt.screenLongOpts(listOf("recursive", "long-option", "with-value=")))
    //println(GetOpt.screenLongOpts(listOf("-")))
    //println(GetOpt.screenLongOpts(listOf("l")))
    //println(GetOpt.screenLongOpts(listOf("-long-option")))
    //println(GetOpt.screenLongOpts(listOf("long-option=a")))
    //println(GetOpt.screenLongOpts(listOf("long-option==")))

    println("\n--- Test getOpt() ---")
    var args: String

    args = "arg1 arg2"
    println("\nargs: $args")
    println(GetOpt.getOpt(args.split(" "), "h"))

    args = "-h "
    println("\nargs: $args")
    println(GetOpt.getOpt(args.split(" "), "h"))

    args = "-i pepe.txt --long-option papo pipo"
    println("\nargs: $args")
    println(GetOpt.getOpt(args.split(" "), "i:", listOf("long-option=")))

    args = "-i pepe.txt -r --long-option papo pipo"
    println("\nargs: $args")
    println(GetOpt.getOpt(args.split(" "), "i:r", listOf("long-option=")))

    args = "-i pepe.txt --recursive --long-option papo --some-flag pipo"
    println("\nargs: $args")
    println(GetOpt.getOpt(args.split(" "), "i:r", listOf("long-option=", "recursive", "some-flag")))
}
