package ua.valeriishymchuk.lobmapeditor.shared.utils

import java.util.*
import java.util.function.ToDoubleFunction
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object StringSimilarityUtils {
    fun getSuggestions(input: String, options: Stream<String>): MutableList<String> {
        return options
            .map<AbstractMap.SimpleEntry<String, Double>> { s: String? ->
                AbstractMap.SimpleEntry<String, Double>(
                    s, jaroDistance(s!!, input)
                )
            }  //.filter(m -> m.getValue() > 0.8)
            .filter { m -> m.value > 0.7 }
            .sorted(Comparator.comparingDouble<AbstractMap.SimpleEntry<String, Double>>
                (ToDoubleFunction { entry: AbstractMap.SimpleEntry<String, Double> -> -entry.value }
                        ))
//            .limit(5)
            .map<String> { it.key }
            .collect(Collectors.toList())
    }

    fun jaroDistance(s1: String, s2: String): Double {
        // If the Strings are equal
        if (s1 === s2) return 1.0

        // Length of two Strings
        val len1 = s1.length
        val len2 = s2.length

        // Maximum distance upto which matching
        // is allowed
        val max_dist = (floor((max(len1, len2) / 2).toDouble()) - 1).toInt()

        // Count of matches
        var match = 0

        // Hash for matches
        val hash_s1: IntArray? = IntArray(s1.length)
        val hash_s2: IntArray? = IntArray(s2.length)

        // Traverse through the first String
        for (i in 0..<len1) {
            // Check if there is any matches

            for (j in max(0, i - max_dist)..<min(len2, i + max_dist + 1))  // If there is a match
                if (s1.get(i) == s2.get(j) && hash_s2!![j] == 0) {
                    hash_s1!![i] = 1
                    hash_s2[j] = 1
                    match++
                    break
                }
        }

        // If there is no match
        if (match == 0) return 0.0

        // Number of transpositions
        var t = 0.0

        var point = 0

        // Count number of occurrences
        // where two characters match but
        // there is a third matched character
        // in between the indices
        for (i in 0..<len1) if (hash_s1!![i] == 1) {
            // Find the next matched character
            // in second String

            while (hash_s2!![point] == 0) point++

            if (s1.get(i) != s2.get(point++)) t++
        }

        t /= 2.0

        // Return the Jaro Similarity
        return (((match.toDouble()) / (len1.toDouble()) + (match.toDouble()) / (len2.toDouble()) + (match.toDouble() - t) / (match.toDouble()))
                / 3.0)
    }
}
