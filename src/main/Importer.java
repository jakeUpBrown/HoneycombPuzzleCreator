package main;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Importer
{
    private static TreeSet wordsInWordList;
    private static TreeSet possibleWordBanks;
    private static List<WordBank> wordBanks;
    private static List<WordBankInfo> favorableWordBanks;
    private static List<Word> wordList;

    private static Pattern nonAlphaPattern = Pattern.compile(Constants.nonAlphaRegex);

    public static void main(String[] args) throws FileNotFoundException {

        long stopwatchStart = System.currentTimeMillis();

        Importer importer = new Importer();

        // Check if created dictionary exists yet. If it does, load that data instead of recreating it.
        if(importer.checkIfFileExists(Constants.localCreatedDictionaryName))
        {
            System.out.println("Local created dictionary found. Loading data");
            // load the created dictionary instead of making one

            if(!importer.loadCreatedDictionary())
            {
                // Could not find created dictionary file.
                System.err.println("Could not find created dictionary file. Something went terribly wrong. ABORTING");
                return;
            }
        }
        else {
            // created dictionary does not exist yet. Must create new one.
            System.out.println("Loading dictionary");

            if(!importer.loadDictionary())
            {
                System.err.println("Could not find dictionary file: " + Constants.dictionaryName + "   ABORTING");
                return;
            }

            if(!importer.loadAllNGrams())
            {
                System.err.println("Could not find all n gram files. ABORTING");
                return;
            }

            PrintStream o = new PrintStream(new File(Constants.createdDictionaryName));
            PrintStream console = System.out;

            System.setOut(o);

            for (Iterator<Word> iter = wordList.iterator(); iter.hasNext(); ) {
                Word newWord = iter.next();
                System.out.println(newWord.getDictionaryWithScoresString());
            }

            System.setOut(console);

            long timeElapsed = (long) ((System.currentTimeMillis() - stopwatchStart) * .001);

            System.out.println("******** Time for creating dictionary with values (sec): " + timeElapsed + " ************");

        }

        System.out.println("Finding eligible bachelors");
        importer.findEligibleMaxWords();
        System.out.println("Setting up word banks");
        importer.setUpWordBanks();

        int max = Integer.MAX_VALUE;
        int i = 0;

        PrintStream o = new PrintStream(new File(Constants.totalSetOutputName));
        PrintStream console = System.out;

        System.out.println("Printing total set to file");

        System.setOut(o);

        long[] scoreFrequency = new long[Constants.scoreLevels.size() + 1];

        for(Iterator<WordBank> iter = wordBanks.iterator(); iter.hasNext(); )
        {
            if(i > max)
            {
                break;
            }

            WordBank bank = iter.next();

            bank.calculateAllValueScores();

            // for each combination
            for(int j = 0; j < Constants.totalLetters; j++) {
                if(bank.getQualityValue(j) > Constants.minBankScore)
                {
                    favorableWordBanks.add(bank.getInfo(j));
                }
            }

            System.setOut(console);
            System.out.println("Writing to file for combination " + bank.getLetters());

            System.setOut(o);
            System.out.println(bank.toPrintString());

            i++;
        }

        o = new PrintStream(new File(Constants.favorableSetOutputName));
        System.setOut(o);

        for(Iterator<WordBankInfo> iter = favorableWordBanks.iterator(); iter.hasNext() ; )
        {
            WordBankInfo bank = iter.next();

            System.out.println(bank.toPrintString());
        }


        System.setOut(console);
        System.out.println(wordBanks.size() + " eligible wordbanks\n\n");

        /*
        for(int j = 0; j < scoreFrequency.length; j++)
        {

            String prefix;
            if(j < Constants.scoreLevels.size())
            {
                prefix = "Number failed at " + Constants.scoreLevels.get(j).getLevelName() + " : ";
            }
            else
            {
                prefix = "Number favorable : ";
            }
            System.out.println(prefix +  scoreFrequency[j]);
        }
        */

        // Get current time, find difference and convert to seconds
        long timeElapsed = (long) ((System.currentTimeMillis() - stopwatchStart) * .001);

        System.out.println("Total time elapsed in seconds: " + timeElapsed);

    }

    public Importer()
    {
        wordsInWordList = new TreeSet();
        possibleWordBanks = new TreeSet();
        wordBanks = new ArrayList<WordBank>();
        wordList = new ArrayList<Word>();
        favorableWordBanks = new ArrayList<WordBankInfo>();
    }

    private boolean checkIfFileExists(String path)
    {
        InputStream res = this.getClass().getResourceAsStream(Constants.localCreatedDictionaryName);

        return res != null;
    }

    /**
     * Loads the dictionary from the dictionaryName file and stores it in the wordList TreeSet
     */
    private boolean loadDictionary()
    {// get text file as resource
        InputStream res = this.getClass().getResourceAsStream(Constants.dictionaryName);

        String str = "";

        try
        {
            // Create BufferedReader to read text file line by line
            BufferedReader reader = new BufferedReader(new InputStreamReader(res));
            if (res != null)
            {
                while((str = reader.readLine()) != null)
                {
                    // only add word if it is above the minimum size.
                    if(str.length() >= Constants.minStringSize) {
                        wordsInWordList.add(str.toUpperCase());
                        insertIntoWordList(new Word(str.toUpperCase()));
                    }
                }
            }
            else
            {
                // file not found. return false
                return false;
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try {res.close(); } catch (Throwable ignore) {}
        }

        return true;
    }

    /**
     * Loads the dictionary from the dictionaryName file and stores it in the wordList TreeSet
     */
    private boolean loadCreatedDictionary()
    {// get text file as resource
        InputStream res = this.getClass().getResourceAsStream(Constants.localCreatedDictionaryName);

        String str = "";

        try
        {
            // Create BufferedReader to read text file line by line
            BufferedReader reader = new BufferedReader(new InputStreamReader(res));
            if (res != null)
            {
                while((str = reader.readLine()) != null)
                {
                    String[] elements = str.split("\t");

                    if(elements.length != 3)
                    {
                        System.err.println("Column found with incorrect length");
                        continue;
                    }
                    Word newWord = new Word(elements[0],
                                            Long.parseLong(elements[1]),
                                            Long.parseLong(elements[2]));

                    wordsInWordList.add(newWord.getWord());
                    insertIntoWordList(newWord);
                }
            }
            else
            {
                // file not found. return false
                return false;
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try {res.close(); } catch (Throwable ignore) {}
        }

        return true;
    }

    private boolean loadAllNGrams()
    {
        // add frequency values to dictionary for every letter
        for(char c = 'a'; c <= 'z'; c++)
        {
            System.out.println("loading N Gram for " + c);
            if(!loadNGram(c))
            {
                // loadNGram returned false. That means the file does not exist
                return false;
            }
        }

        return true;
    }

    /**
     * Loads the nGram count values from the nGramFileName file and stores it in the wordList TreeSet
     */
    private boolean loadNGram(char c)
    {// get text file as resource
        InputStream res = this.getClass().getResourceAsStream(Constants.getNGramFileName(c));

        String str = "";

        // currentLineUnprocessed means that the value that str holds has been read, but the values have not been used
        // this occurs if the line is read, but a different word is found in the word column.
        boolean currentLineUnprocessed = true;

        boolean moreLines = true;
        try
        {
            // Create BufferedReader to read text file line by line
            BufferedReader reader = new BufferedReader(new InputStreamReader(res));
            if (res != null)
            {
                // continue through until no more lines exist
                while(moreLines)
                {
                    // if the current line is unprocessed then reading a new line is skipped. If the processing has been done, read a new line.
                    if(!currentLineUnprocessed) {

                        // if here, it means that the current str value has been processed. Read new one.
                        str = reader.readLine();

                        // if str == null, then there are no more lines in the file. exit loop.
                        if(str == null)
                        {
                            System.out.println("str == null");
                            moreLines = false;
                            continue;
                        }
                    }

                    // process line and split into strings. Will return null if invalid word
                    String[] elements = processNGramLine(str);

                    if(elements == null)
                    {
                        // if null is returned, the word was invalid. Continue onto the next line
                        currentLineUnprocessed = false;
                        continue;
                    }

                    String word = elements[Constants.nGramCols.WORD.ordinal()];

                    // Check if the word is found in the dictionary
                    boolean wordFound = wordsInWordList.contains(word);

                    // Keep a tally of count and volume frequencies
                    long countFrequency = 0;
                    long volumeFrequency = 0;

                    do {

                        // only keep tally if word is relevant. If not relevant, this section is used to skip all lines for the same, unfound word.
                        if(wordFound)
                        {
                            countFrequency += Integer.parseInt(elements[Constants.nGramCols.MATCHCOUNT.ordinal()]);
                            volumeFrequency += Integer.parseInt(elements[Constants.nGramCols.VOLUMECOUNT.ordinal()]);
                        }

                        // Read the next line
                        str = reader.readLine();

                        if(str == null)
                        {
                            System.out.println("str == null");
                            moreLines = false;
                            break;
                        }

                        elements = processNGramLine(str);

                        // if elements == null, the word was invalid and must be a new word
                        if(elements == null)
                        {
                            continue;
                        }

                    }
                    while(elements != null && elements[Constants.nGramCols.WORD.ordinal()].equals(word));

                    if(elements == null)
                    {
                        // if elements == null, then the last line must be and invalid word, which requires no further processing
                        currentLineUnprocessed = false;
                    }
                    else
                    {
                        // elements exist, but the word is different, so the newest read line is unprocessed.
                        currentLineUnprocessed = true;
                    }

                    // if the word exists in the dictionary, add the count and volume frequencies to their collective values.
                    if(wordFound) {

                        Word newWord = new Word(word, countFrequency, volumeFrequency);

                        int index = binarySearchWordList(newWord);
                        if(!wordList.get(index).getWord().equals(word))
                        {
                            System.err.println("Something went wrong: " + word + " does not match " + wordList.get(index).getWord());
                        }
                        else
                        {
                            wordList.get(index).addToCountFrequency(countFrequency);
                            wordList.get(index).addToVolumeFrequency(volumeFrequency);

                        }


                    }

                }
            }
            else
            {
                // file not found. return false.
                return false;
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try {res.close(); } catch (Throwable ignore) {}
        }

        return true;
    }


    private void insertIntoWordList(Word word)
    {
        wordList.add(getWordListIndexToInsert(word), word);
    }

    private int getWordListIndexToInsert(Word word)
    {
        int index = binarySearchWordList(word);

        return Math.abs(index + 1);
    }

    private int binarySearchWordList(Word word)
    {
        // find index where it should inserted
        return Collections.binarySearch(wordList, word, new Comparator<Word>() {
            @Override
            public int compare(Word o1, Word o2) {
                return o1.getWord().compareTo(o2.getWord());
            }
        });
    }

    public String[] processNGramLine(String line)
    {

        if(line == null)
        {
            return null;
        }

        // split element by tab
        String[] elements = line.split("\t");

        if(elements.length != Constants.nGramColCount)
        {
            // Found a misformed line in nGram files
            System.err.println("Found column with more than " + Constants.nGramColCount + " columns");
            // Skip. Shouldn't be a problem.
            return null;
        }

        // truncate the word at the underscore. This is because we are not interested in
        // specifics of word use, which is what is indicated after the underscore

        elements[Constants.nGramCols.WORD.ordinal()] = elements[Constants.nGramCols.WORD.ordinal()].split("_")[0].toUpperCase();

        // ignore word if they are not alphabetical. There are number of words with ' and . in it, which we know are no in the scrabble dictionary
        Matcher m = nonAlphaPattern.matcher(elements[Constants.nGramCols.WORD.ordinal()]);

        if(m.find())
        {
            return null;
        }

        return elements;
    }

    /**
     * used to find total words that have the maximum number of unique letters
     */
    private void findEligibleMaxWords()
    {
        for(Iterator<String> iter = wordsInWordList.iterator(); iter.hasNext(); )
        {
            String word = iter.next();
            if(hasCorrectUniqueLetters(word)) {

                StringBuffer uniqueLettersKey = new StringBuffer();
                char lowest = Character.MAX_VALUE;
                char prevLowest = (char) 0;
                for (int i = 0; i < Constants.totalLetters; i++)
                {

                    for(int j = 0; j < word.length(); j++)
                    {
                        char letterIter = word.charAt(j);

                        if(letterIter < lowest && letterIter > prevLowest)
                        {
                            // found new lowest letter.
                            lowest = letterIter;
                        }
                    }

                    // found next lowest letter. add to string
                    uniqueLettersKey.append(lowest);
                    prevLowest = lowest;
                    lowest = Character.MAX_VALUE;
                }

                if(uniqueLettersKey.length() != Constants.totalLetters)
                {
                    System.out.println("Found a string that's not the appropriate letter length");
                    System.exit(0);
                }
                possibleWordBanks.add(uniqueLettersKey.toString());
            }
        }

    }

    /**
     * returns true if word has desired number of unique letters
     * @param word
     * @return
     */
    private boolean hasCorrectUniqueLetters(String word)
    {
        return word.chars().distinct().count() == Constants.totalLetters;
    }

    /**
     * Sets up the actual word banks using the set of possible word banks.
     * This includes finding the answer key for every combination of the 7 words (i.e. if any particular word was placed in the middle)
     */
    private void setUpWordBanks()
    {
        // iterate through treeSet of possibleWordBanks
        // for each bank, find all words that are eligible.
        for(Iterator<String> bankIter = possibleWordBanks.iterator(); bankIter.hasNext(); )
        {

            WordBank newBank = new WordBank(bankIter.next());

            System.out.println("Setting up wordbank for letters: " + newBank.getLetters());

            List<Word> bigWordList = new ArrayList<Word>();

            for(Iterator<Word> dictIter = wordList.iterator(); dictIter.hasNext(); )
            {
                Word word = dictIter.next();
                if(onlyBankLettersUsed(word.getWord(), newBank.getLetters())){
                    // Keep big list using any of the letters. Will sort them into buckets of mandatory letters later
                    bigWordList.add(word);
                }
            }

            // if they use 7 letters, add them to the allLetterWord.
            // Using iterator in order to remove the allLetterWords
            for(Iterator<Word> bigListIter = bigWordList.iterator(); bigListIter.hasNext(); )
            {
                Word listWord = bigListIter.next();

                if(listWord.getWord().chars().distinct().count() == Constants.totalLetters)
                {
                    // Found a word that uses all letters, add to allLetterWords and remove from list.
                    newBank.addAllLetterWord(listWord);
                    bigListIter.remove();
                }
            }

            // iterate through big word list and add to appropriate buckets whether they're eligible for each mandatory letter
            List<Word>[] mandatoryLetterBanks = (ArrayList<Word>[]) new ArrayList[Constants.totalLetters];

            for(int i = 0; i < Constants.totalLetters; i++)
            {
                mandatoryLetterBanks[i] = new ArrayList<Word>();
            }

            for(Word matchingWord : bigWordList)
            {
                for(int i = 0; i < newBank.getLetters().length(); i++)
                {
                    if(matchingWord.getWord().indexOf(newBank.getLetters().charAt(i)) >= 0)
                    {
                        // letter was found in the matchingWord.
                        // Add to associated buckets
                        mandatoryLetterBanks[i].add(matchingWord);
                    }
                }
            }

            newBank.setMandatoryLetterBanks(mandatoryLetterBanks);

            wordBanks.add(newBank);
        }
    }


    /**
     * Checks if all of the letters inside of word are included in the bank.
     * @param word
     * @param bank
     * @return
     */
    private boolean onlyBankLettersUsed(String word, String bank)
    {
        for(int wordIter = 0; wordIter < word.length(); wordIter++)
        {
            boolean found = false;

            for(int bankIter = 0; bankIter < bank.length(); bankIter++)
            {
                if(word.charAt(wordIter) == bank.charAt(bankIter))
                {
                    found = true;
                    break;
                }
            }

            if(!found)
            {
                return false;
            }
        }

        return true;
    }

}
