/*
 * Copyright Hank Adler 2020
 */

package com.hankadler.io

import java.security.InvalidParameterException
import javax.swing.text.StyledEditorKit
import kotlin.system.exitProcess

/**
 * C-style parser for command line options.
 *
 * <p>Emulates the ``getopt`` module from the Python Standard Library, with minor exceptions, the most obvious of which
 * is that this ``getOpt`` is equivalent to Python's ``gnu_getopt`` function.
 *
 * @see <a href="https://docs.python.org/3.8/library/getopt.html"> getopt </a>
 *
 * @author  Hank Adler
 * @version 0.3.0
 * @license MIT
 */
object GetOpt {
    /**
     * Optional arguments.
     *
     * <p>Keys are option identifiers:
     * <ul>
     *     <li>Short Options: Options of the form -<letter> (Example: -h)</li>
     *     <li>Long Options: Options of the form --<word> (Example: --help)</li>
     * </ul>
     *
     * <p>Values are option-values given immediately after in `argv`. (Example: -i file.txt)
     */
    private val opts: MutableMap<String, String?> = mutableMapOf()

    /**
     * Required arguments. (`argv` minus `opts` and option-values)
     */
    private val args: MutableList<String> = mutableListOf()

    /**
     * An options to criteria dictionary.
     *
     * <p>Options are optional arguments as they would appear in `argv`.
     * Examples: "-r", "--recursive", "--human-readable"
     *
     * <p>Criteria are the optional arguments that are recognized by ``GetOpt`` as they are specified in ``shortOpts``
     * and ``longOpts``. Examples: "r", "recursive", "human-readable="
     */
    private val optsToCriteria: MutableMap<String, String> = mutableMapOf()

    private fun init() {
        opts.clear()
        args.clear()
        optsToCriteria.clear()
    }

    @Override
    fun getOpt(
            argv: Array<String>, shortOpts: String = "", longOpts: List<String> = emptyList()
    ): Pair<Map<String, String?>, Array<String>> {
        init()
        try {
            return getOpt(argv, parseShortOpts(shortOpts), parseLongOpts(longOpts))
        } catch (e: Exception) {
            println("\nERROR: " + e.message)
            exitProcess(1)
        }
    }

    /**
     * Parses `argv` into ``opts`` and ``args``.
     *
     * @param  argv Arguments vector.
     * @param  shortOpts List of letters with or without ':' that represent short options.
     *         <p>Example: "hri:" is used to interpret '-h', '-r' and '-i <value>'.
     * @param  longOpts List of words with or without '=' that represent long options.
     *         <p>Example: ["help", "some-option="] is used to interpret '--help', '--some-option <value>'.
     * @throws GetOptError if an unrecognized option is found on `argv` or when no value is given to an option requiring
     *         one.
     * @return An options-arguments pair corresponding to ``opts`` and ``args`` properties, respectively.
     */
    private fun getOpt(
            argv: Array<String>, shortOpts: List<String> = emptyList(), longOpts: List<String> = emptyList()
    ): Pair<Map<String, String?>, Array<String>> {
        argv.forEach {
            if (isRecognizedOpt(it, shortOpts, longOpts)) {
                // The following check is needed because isRecognizedOpt() may add `it` to ``opts`` if it finds that
                // `it` is a short-option with an embedded option-value.
                if (it !in opts.keys) {
                    if (isShortOpt(it[0].toString()) && it.length == 2) {
                        opts[it] = null  // Initialization.
                    }
                }

                // The second expression in the following conditional statement avoids overriding any option-value
                // already set by isRecognizedOpt().
                if (requiresOptValue(it) && opts[it] == null) {
                    try {
                        val nextArg = argv[argv.indexOf(it) + 1]
                        if (isRecognizedOpt(nextArg, shortOpts, longOpts)) {
                            throw GetOptError("$nextArg is not a valid value for $it!")
                        }
                        opts[it] = nextArg
                    } catch (e: IndexOutOfBoundsException) {
                        throw GetOptError("$it option requires a value!")
                    }
                }
            } else if (isArg(it)) {
                args.add(it)
            }
        }

        return opts.toMap() to args.toTypedArray()
    }

    /**
     * Checks whether `arg` is recognized as an option in `shortOpts` or `longOpts`. If it is, ``optsToCriteria``
     * property is updated with appropriate mapping.
     *
     * @param  arg Argument in `argv` being parsed.
     * @param  shortOpts List of letters with or without ':' that represent short options.
     * @param  longOpts List of words with or without '=' that represent long options.
     * @throws GetOptError if an unrecognized option is found on `argv` or when no value is given to an option requiring
     * @return True or false.
     */
    private fun isRecognizedOpt(arg: String, shortOpts: List<String>, longOpts: List<String>): Boolean {
        if (!arg.startsWith("-")) {
            return false
        }

        // Captures [prefix][suffix] from `arg`; prefix becomes "-" or "--" and suffix the remaining allowed characters.
        val matchedGroups = Regex("(^--?)([\\w-.]+)").matchEntire(arg)?.groups
        val prefix = matchedGroups?.get(index = 1)?.value!!
        var suffix = matchedGroups.get(index = 2)?.value
        suffix = suffix ?: throw GetOptError("ERROR: Invalid option $arg!")

        // Performs a series of syntactical evaluations on `arg` (option).
        if (!startsWithLetter(suffix)) {
            throw GetOptError("ERROR: $arg option suffix must begin with a letter!")
        }
        if (!isUniqueOpt(prefix, suffix)) {
            throw GetOptError("ERROR: $arg option was already provided!")
        }
        if (isShortOpt(prefix)) {
            if (containsEmbeddedValue(suffix)) {
                opts[prefix + suffix.substring(0, 1)] = suffix.substring(1)
                suffix = suffix.substring(0, 1)  // Removes embedded value from suffix.
            }
        }

        // Checks if `arg` matches one in `shortOpts` or `longOpts`.
        (shortOpts + longOpts).forEach {
            if (it.removeSuffix(":").removeSuffix("=") == suffix) {
                optsToCriteria[prefix + suffix] = it
                return true
            }
        }

        throw GetOptError("$arg option is not recognized!")
    }

    private fun startsWithLetter(string: String) = Regex("^[a-zA-Z].*").matches(string)

    private fun isUniqueOpt(prefix: String, suffix: String): Boolean {
        val opt = prefix + suffix
        if (isShortOpt(prefix)) {
            opts.keys.forEach {
                if (it.contentEquals(opt.slice(0..1))) {
                    return false
                }
            }
        } else {
            opts.keys.forEach {
                if (it.contentEquals(suffix)) {
                    return false
                }
            }
        }
        return true
    }

    private fun isShortOpt(prefix: String) = prefix == "-"

    private fun containsEmbeddedValue(suffix: String) = suffix.length > 1

    private fun requiresOptValue(arg: String): Boolean {
       return  optsToCriteria[arg]?.endsWith(':') ?: false || optsToCriteria[arg]?.endsWith('=') ?: false
    }

    private fun isArg(arg: String) = arg !in opts.values

    /**
     * Converts `shortOpts` to list comprised of each letter plus any colon after it.
     *
     * @param shortOpts String of options letters, with options requiring an argument followed by a ":" (colon).
     * @return          List of short options criteria.
     */
    internal fun parseShortOpts(shortOpts: String): List<String> {
        if (shortOpts.isBlank()) {
            return emptyList()
        }

        val result: MutableList<String> = mutableListOf()
        Regex("\\w:?").findAll(shortOpts).forEach { result.add(it.value) }

        return result
    }

    /**
     * Ensures non-empty `longOpts` meet the following requirements:
     * <ol>
     *     <li>Only letters, numbers, '_', '-' and '=' allowed.</li>
     *     <li>Only one '=' allowed.</li>
     *     <li>Strings must start with a letter</li>
     *     <li>Strings must end with a letter or '='</li>
     *     <li>Strings must be at least two characters long, excluding '='.</li>
     *     <li>'-' can only exist between letters or numbers.</li>
     *     <li>No two consecutive '-' allowed.</li>
     *     <li>No spaces allowed.</li>
     * </ol>
     *
     * @param longOpts List of long options to be recognized by ``getOpt``. Leading '--' characters should not be
     *        included. Options requiring an argument should be followed by '='.
     * @throws IllegalArgumentException if `longOpts` does not meet the above requirements.
     * @return Valid long options.
     */
    internal fun parseLongOpts(longOpts: List<String>): List<String> {
        if (longOpts.isEmpty()) {
            return longOpts
        }

        longOpts.forEach {
            if (!Regex("^[a-zA-Z]\\w+(?>-?\\w)*=?\$").matches(it)) {
                throw IllegalArgumentException("ERROR: Invalid long option '$it'!")
            }
        }

        return longOpts
    }
}
