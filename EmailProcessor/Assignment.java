import java.util.*;

public class Assignment {
    public static void main(String args[]){
    }
    public class MaxOfThree {
        public static int findMax(int num1, int num2, int num3) {
            return (num1 > num2) ? (num1 > num3 ? num1 : num3) : (num2 > num3 ? num2 : num3);
        }
    }

    public class EvenOddCheck {
        public static boolean isEven(int num) {
            return num % 2 == 0;
        }
    }

    public class Factorial {
        public static long calculateFactorial(int num) {
            long factorial = 1;
            for (int i = 1; i <= num; i++) {
                factorial *= i;
            }
            return factorial;
        }
    }

    public class PatternPrint {
        public static void printPattern(int n) {
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= i; j++) {
                    System.out.print("* ");
                }
                System.out.println();
            }
        }
    }

    public class AddBinary {
        public static String addBinary(String bin1, String bin2) {
            int num1 = Integer.parseInt(bin1, 2);
            int num2 = Integer.parseInt(bin2, 2);
            int sum = num1 + num2;
            return Integer.toBinaryString(sum);
        }
    }

    public class ComplexNumbers {
        public static class Complex {
            int real;
            int imaginary;

            Complex(int r, int i) {
                this.real = r;
                this.imaginary = i;
            }
        }

        public static Complex addComplex(Complex c1, Complex c2) {
            return new Complex(c1.real + c2.real, c1.imaginary + c2.imaginary);
        }
    }

    public class MultiplyNumbers {
        public static int multiply(int num1, int num2) {
            return num1 * num2;
        }
    }

    public class LeapYearCheck {
        public static boolean isLeapYear(int year) {
            if (year % 4 == 0) {
                if (year % 100 == 0) {
                    return year % 400 == 0;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    public class VowelConsonantCheck {
        public static boolean isVowel(char ch) {
            ch = Character.toLowerCase(ch);
            return ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u';
        }
    }

    public class CompoundInterest {
        public static double calculateCompoundInterest(double principal, double rate, int time, int n) {
            return principal * Math.pow(1 + (rate / n), n * time);
        }
    }

    public class SimpleInterest {
        public static double calculateSimpleInterest(double principal, double rate, int time) {
            return (principal * rate * time) / 100;
        }
    }

    public class PowerOfNumber {
        public static double calculatePower(double base, int exponent) {
            return Math.pow(base, exponent);
        }
    }

    public class CharStringConversion {
        public static String charToString(char ch) {
            return String.valueOf(ch);
        }

        public static char stringToChar(String str) {
            if (str != null && str.length() > 0) {
                return str.charAt(0);
            }
            throw new IllegalArgumentException("String is null or empty");
        }
    }



    public class PalindromeCheck {
        public static boolean isPalindromeUsingStack(String str) {
            Stack<Character> stack = new Stack<>();
            for (char ch : str.toCharArray()) {
                stack.push(ch);
            }
            for (char ch : str.toCharArray()) {
                if (ch != stack.pop()) {
                    return false;
                }
            }
            return true;
        }

        public static boolean isPalindromeUsingQueue(String str) {
            Queue<Character> queue = new LinkedList<>();
            for (char ch : str.toCharArray()) {
                queue.add(ch);
            }
            for (int i = str.length() - 1; i >= 0; i--) {
                if (str.charAt(i) != queue.poll()) {
                    return false;
                }
            }
            return true;
        }

        public static boolean isPalindromeUsingFor(String str) {
            int n = str.length();
            for (int i = 0; i < n / 2; i++) {
                if (str.charAt(i) != str.charAt(n - i - 1)) {
                    return false;
                }
            }
            return true;
        }

        public static boolean isPalindromeUsingWhile(String str) {
            int i = 0, j = str.length() - 1;
            while (i < j) {
                if (str.charAt(i) != str.charAt(j)) {
                    return false;
                }
                i++;
                j--;
            }
            return true;
        }
    }


    public class SortStrings {
        public static String[] sortStrings(String[] arr) {
            Arrays.sort(arr);
            return arr;
        }
    }

    public class ReverseWords {
        public static String reverseWords(String str) {
            String[] words = str.split(" ");
            StringBuilder reversedString = new StringBuilder();
            for (int i = words.length - 1; i >= 0; i--) {
                reversedString.append(words[i]).append(" ");
            }
            return reversedString.toString().trim();
        }
    }

    public class BubbleSortStrings {
        public static String[] bubbleSort(String[] arr) {
            int n = arr.length;
            String temp;
            for (int i = 0; i < n - 1; i++) {
                for (int j = 0; j < n - i - 1; j++) {
                    if (arr[j].compareTo(arr[j + 1]) > 0) {
                        temp = arr[j];
                        arr[j] = arr[j + 1];
                        arr[j + 1] = temp;
                    }
                }
            }
            return arr;
        }
    }

    public class CharOccurrence {
        public static int findOccurrence(String str, char ch) {
            int count = 0;
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == ch) {
                    count++;
                }
            }
            return count;
        }
    }

    public class VowelConsonantCount {
        public static int[] countVowelsAndConsonants(String str) {
            int[] counts = {0, 0}; // counts[0] is vowels, counts[1] is consonants
            for (char ch : str.toLowerCase().toCharArray()) {
                if (ch >= 'a' && ch <= 'z') {
                    if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u') {
                        counts[0]++;
                    } else {
                        counts[1]++;
                    }
                }
            }
            return counts;
        }
    }


    public class AnagramCheck {
        public static boolean isAnagram(String str1, String str2) {
            char[] arr1 = str1.toCharArray();
            char[] arr2 = str2.toCharArray();
            Arrays.sort(arr1);
            Arrays.sort(arr2);
            return Arrays.equals(arr1, arr2);
        }
    }

    public class DivideString {
        public static String[] divideString(String str, int n) {
            int len = str.length();
            int partSize = len / n;
            String[] parts = new String[n];

            if (len % n != 0) {
                throw new IllegalArgumentException("String cannot be divided into " + n + " equal parts");
            }

            for (int i = 0; i < n; i++) {
                parts[i] = str.substring(i * partSize, (i + 1) * partSize);
            }
            return parts;
        }
    }

    public class Subsets {
        public static List<String> findSubsets(String str) {
            List<String> subsets = new ArrayList<>();
            int n = str.length();
            for (int i = 0; i < (1 << n); i++) {
                StringBuilder subset = new StringBuilder();
                for (int j = 0; j < n; j++) {
                    if ((i & (1 << j)) != 0) {
                        subset.append(str.charAt(j));
                    }
                }
                subsets.add(subset.toString());
            }
            return subsets;
        }
    }


    public class Subsets2 {
        public static List<String> findSubsets(String str) {
            List<String> subsets = new ArrayList<>();
            int n = str.length();
            for (int i = 0; i < (1 << n); i++) {
                StringBuilder subset = new StringBuilder();
                for (int j = 0; j < n; j++) {
                    if ((i & (1 << j)) != 0) {
                        subset.append(str.charAt(j));
                    }
                }
                subsets.add(subset.toString());
            }
            return subsets;
        }
    }

    public class LongestSubstring {
        public static String findLongestSubstring(String str) {
            String longest = "";
            for (int i = 0; i < str.length(); i++) {
                Set<Character> seen = new HashSet<>();
                StringBuilder current = new StringBuilder();
                for (int j = i; j < str.length(); j++) {
                    char ch = str.charAt(j);
                    if (seen.contains(ch)) {
                        break;
                    }
                    seen.add(ch);
                    current.append(ch);
                }
                if (current.length() > longest.length()) {
                    longest = current.toString();
                }
            }
            return longest;
        }
    }

    public class LongestRepeatingSequence {
        public static String findLongestRepeatingSequence(String str) {
            int n = str.length();
            String result = "";
            int[][] lcs = new int[n + 1][n + 1];

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    if (str.charAt(i - 1) == str.charAt(j - 1) && i != j) {
                        lcs[i][j] = lcs[i - 1][j - 1] + 1;
                        if (lcs[i][j] > result.length()) {
                            result = str.substring(i - lcs[i][j], i);
                        }
                    } else {
                        lcs[i][j] = 0;
                    }
                }
            }
            return result;
        }
    }
    public class RemoveWhiteSpaces {
        public static String removeWhiteSpaces(String str) {
            return str.replaceAll("\\s", "");
        }
    }

    public class ArrayElementCount {
        public static int countElements(int[] arr) {
            return arr.length;
        }
    }

    public class AverageOfArray {
        public static double calculateAverage(int[] arr) {
            int sum = 0;
            for (int num : arr) {
                sum += num;
            }
            return (double) sum / arr.length;
        }
    }

    public class SumOfArray {
        public static int sumElements(int[] arr) {
            int sum = 0;
            for (int num : arr) {
                sum += num;
            }
            return sum;
        }
    }

    public class ReverseArray {
        public static int[] reverseArray(int[] arr) {
            int n = arr.length;
            for (int i = 0; i < n / 2; i++) {
                int temp = arr[i];
                arr[i] = arr[n - i - 1];
                arr[n - i - 1] = temp;
            }
            return arr;
        }
    }

    public class SortArray {
        public static int[] sortArray(int[] arr) {
            Arrays.sort(arr);
            return arr;
        }
    }

    public class CharArrayToString {
        public static String convertCharArrayToString(char[] charArray) {
            return new String(charArray);
        }
    }


    public class RemoveDuplicateCharacters {
        public static String removeDuplicates(String str) {
            Set<Character> seen = new HashSet<>();
            StringBuilder result = new StringBuilder();
            for (char ch : str.toCharArray()) {
                if (!seen.contains(ch)) {
                    seen.add(ch);
                    result.append(ch);
                }
            }
            return result.toString();
        }
    }

    public class LongestPalindrome {
        public static String findLongestPalindrome(String s) {
            if (s == null || s.length() < 1) return "";
            int start = 0, end = 0;
            for (int i = 0; i < s.length(); i++) {
                int len1 = expandAroundCenter(s, i, i);
                int len2 = expandAroundCenter(s, i, i + 1);
                int len = Math.max(len1, len2);
                if (len > end - start) {
                    start = i - (len - 1) / 2;
                    end = i + len / 2;
                }
            }
            return s.substring(start, end + 1);
        }

        private static int expandAroundCenter(String s, int left, int right) {
            while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
                left--;
                right++;
            }
            return right - left - 1;
        }
    }







}
