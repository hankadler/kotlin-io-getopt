/*
 * Copyright Hank Adler 2020
 */

package com.hankadler.io

import java.security.InvalidParameterException

/**
 * C-style parser for command line options.
 *
 * Emulates the getopt module from the Python Standard Library.
 * @see <a href="https://docs.python.org/3.8/library/getopt.html"> getopt </a>
 *
 * @author  Hank Adler
 * @version 0.1.0
 * @license MIT
 */
object GetOpt {
    /**
     * Parses `args` such that its elements fall in one of two collections:
     * <ol>
     *     <li>arguments: Arguments that should always be provided. (`args` minus options and option values)</li>
     *     <li>options: Optional arguments. These fall into one of two categories:</li>
     *         <ul>
     *             <li>Short Options: Options of the form -<letter> (Example: -h)</li>
     *             <li>Long Options: Options of the form --<word> (Example: --help)</li>
     *
     *             Options may require values to be specified immediately after (Example: -i file.txt).
     *         </ul>
     * </ol>
     *
     * @param args      List of all arguments to a command, excluding the command.
     * @param shortOpts String of letters that represent short options.
     *                  Example: "hri:" is used to interpret '-h', '-r' and '-i <value>'.
     * @param longOpts  List of words that represent long options.
     *                  Example: ["help", "some-option="] is used to interpret '--help', '--some-option <value>'.
     * @return          A arguments-options pair, where arguments is a list reduced to non-optional values
     *                  (required arguments) and options is a map of optional arguments (long or short) and their
     *                  values, if any.
     */
    fun getOpt(
        args: List<String>,
        shortOpts: String,
        longOpts: List<String> = emptyList()
    ): Pair<List<String>, Map<String, String?>> {
        /* Initializes return values. */
        val arguments = args.toMutableList()  // This will be subsequently mutated to contain required arguments only.
        val options = mutableMapOf<String, String?>()

        /* Validates parameters. */
        screenLongOpts(longOpts)
        val shortOptsList = screenShortOpts(shortOpts)
        screenArgs(args, shortOptsList, longOpts)

        /* Parses `args` into ``arguments`` and ``options`` */
        var optionKey: String
        var optionValue: String?
        for (optionCriteria in (shortOptsList + longOpts)) {
            for (arg in args) {
                if (arg !in arguments) {
                    continue
                }
                /* Capturing groups are as follows:
                 *     0: Full match.
                 *     1: Short option letter without '-' prefix.
                 *     2: Short option value, if no space exists after short option letter.
                 *     3: Long option word without '--' prefix.
                 */
                val regex = Regex("^-(\\w)(\\w*)|^--(\\w[\\w-]*)")
                val capturedGroups = regex.matchEntire(arg)?.groups

                if (capturedGroups != null) {
                    optionValue = null

                    if (!capturedGroups[1]?.value.isNullOrBlank()) {
                        optionKey = "-" + capturedGroups[1]!!.value

                        if (optionCriteria.endsWith(':')) {
                            if (!capturedGroups[2]?.value.isNullOrBlank()) {
                                optionValue = capturedGroups[2]!!.value
                                arguments.remove(optionKey + optionValue)
                            } else {
                                optionValue = args[args.indexOf(arg) + 1]
                                arguments.remove(optionValue)
                            }
                        }
                    } else {
                        optionKey = "--" + capturedGroups[3]!!.value

                        if (optionCriteria.endsWith('=')) {
                            optionValue = args[args.indexOf(arg) + 1]
                            arguments.remove(optionValue)
                        }
                    }

                    arguments.remove(optionKey)  // Does nothing if short option value is included in `arg`.
                    options[optionKey] = optionValue

                    break
                }
            }
        }

        return arguments.toList() to options.toMap()
    }

    /**
     * Returns a list where each element is comprised of the short option
     * (letter) in `shortOps` plus any colon given after the letter. If
     * `shortOpts` is blank or empty, an empty list is returned.
     *
     * @param shortOpts String of options letters, with options requiring
     *                  an argument followed by a ":" (colon).
     * @return          List of short option letters combined with their
     *                  option argument flag (colon), if any.
     */
     internal fun screenShortOpts(shortOpts: String): List<String> {
        if (shortOpts.isBlank()) {
            return emptyList()
        }

        val result: MutableList<String> = mutableListOf()
        Regex("\\w:?").findAll(shortOpts).forEach { result.add(it.value) }

        return result
    }

    /**
     * Ensures `longOpts` contains only letters, dashes '-' and/or equal
     * signs '=' if non-empty.
     *
     * @param longOpts List of long options (words) supported. The
     *                 leading '--' characters should not be included.
     *                 Long options requiring an argument should be
     *                 followed by an equal sign ('=').
     * @return         Valid long options.
     */
    internal fun screenLongOpts(longOpts: List<String>): List<String> {
        if (longOpts.isEmpty()) {
            return emptyList()
        }

        longOpts.forEach {
            if (!it.contains(regex = Regex("[a-zA-Z]-?=?")) || it[0] == '-') {
                throw InvalidParameterException(
                        "ERROR: --$it must start with letters, optionally followed by one '-' or '='!")
            }
            if (it[it.lastIndex] == '-') {
                throw InvalidParameterException("ERROR: --$it cannot end on a '-'!")
            }
            if (it.count { c -> c == '='} > 1) {
                throw InvalidParameterException("ERROR: --$it must contain only one '='!")
            }
            if (it.contains('=') && it[it.lastIndex] != '=') {
                throw InvalidParameterException("ERROR: --$it must end on '='!")
            }
            if (it.length == 1) {
                throw InvalidParameterException("ERROR: --$it must contain more than one letter!")
            }
        }

        return longOpts
    }

    /**
     * Checks `args` options against `shortOptsList` and `longOpts` and verifies that no unrecognized option is given.
     *
     * @param args          List of all arguments to a command, excluding the command.
     * @param shortOptsList List of letters that represent short options.
     * @param longOpts      List of words that represent long options.
     */
    private fun screenArgs(args: List<String>, shortOptsList: List<String>, longOpts: List<String>) {
        var isOptionValid = true

        args.forEach {
            if (it.startsWith('-')) {
                isOptionValid = false
                for (shortOpt in shortOptsList) {
                    if ("-$shortOpt".contains(it)) {
                        isOptionValid = true
                        break
                    }
                }
                for (longOpt in longOpts) {
                    if ("--$longOpt".contains(it)) {
                        isOptionValid = true
                        break
                    }
                }
            }

            if (!isOptionValid) {
                throw Exception("ERROR: ``$it`` is not a recognized option!")
            }
        }
    }
}
