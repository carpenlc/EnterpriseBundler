package mil.nga.util;

import java.util.Random;

public class JobIDGenerator {

    /**
     * Generate a random hex encoded string token of the specified length.
     * Since there are two hex characters per byte, the random hex string 
     * returned will be twice as long as the user-specified length.
     *  
     * @param length The number of random bytes to use
     * @return random hex string
     */
    public static synchronized String generateUniqueToken(int length) {

        byte         random[]        = new byte[length];
        Random       randomGenerator = new Random();
        StringBuffer buffer          = new StringBuffer();

        randomGenerator.nextBytes(random);

        for (int j = 0; j < random.length; j++)
        {
            byte b1 = (byte) ((random[j] & 0xf0) >> 4);
            byte b2 = (byte) (random[j] & 0x0f);
            if (b1 < 10)
                buffer.append((char) ('0' + b1));
            else
                buffer.append((char) ('A' + (b1 - 10)));
            if (b2 < 10)
                buffer.append((char) ('0' + b2));
            else
                buffer.append((char) ('A' + (b2 - 10)));
        }

        return (buffer.toString());
    }

}
