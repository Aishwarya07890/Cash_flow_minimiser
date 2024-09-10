import java.util.*;

class Bank {
    public String name;
    public int netAmount;
    public Set<String> types = new HashSet<>();
}

public class CashFlowMinimizer {

    public static int getMinIndex(Bank[] listOfNetAmounts, int numBanks) {
        int min = Integer.MAX_VALUE, minIndex = -1;
        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;
            if (listOfNetAmounts[i].netAmount < min) {
                minIndex = i;
                min = listOfNetAmounts[i].netAmount;
            }
        }
        return minIndex;
    }

    public static int getSimpleMaxIndex(Bank[] listOfNetAmounts, int numBanks) {
        int max = Integer.MIN_VALUE, maxIndex = -1;
        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;
            if (listOfNetAmounts[i].netAmount > max) {
                maxIndex = i;
                max = listOfNetAmounts[i].netAmount;
            }
        }
        return maxIndex;
    }

    public static Pair<Integer, String> getMaxIndex(Bank[] listOfNetAmounts, int numBanks, int minIndex, Bank[] input, int maxNumTypes) {
        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        String matchingType = null;

        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0 || listOfNetAmounts[i].netAmount < 0) continue;

            List<String> v = new ArrayList<>(maxNumTypes);
            Set<String> intersection = new HashSet<>(listOfNetAmounts[minIndex].types);
            intersection.retainAll(listOfNetAmounts[i].types);

            if (!intersection.isEmpty() && max < listOfNetAmounts[i].netAmount) {
                max = listOfNetAmounts[i].netAmount;
                maxIndex = i;
                matchingType = intersection.iterator().next();
            }
        }
        return new Pair<>(maxIndex, matchingType);
    }

    public static void printAns(List<List<Pair<Integer, String>>> ansGraph, int numBanks, Bank[] input) {
        System.out.println("\nThe transactions for minimum cash flow are as follows: \n");
        for (int i = 0; i < numBanks; i++) {
            for (int j = 0; j < numBanks; j++) {
                if (i == j) continue;

                Pair<Integer, String> iToJ = ansGraph.get(i).get(j);
                Pair<Integer, String> jToI = ansGraph.get(j).get(i);

                if (iToJ.getFirst() != 0 && jToI.getFirst() != 0) {
                    if (iToJ.getFirst().equals(jToI.getFirst())) {
                        ansGraph.get(i).set(j, new Pair<>(0, ""));
                        ansGraph.get(j).set(i, new Pair<>(0, ""));
                    } else if (iToJ.getFirst() > jToI.getFirst()) {
                        ansGraph.get(i).set(j, new Pair<>(iToJ.getFirst() - jToI.getFirst(), iToJ.getSecond()));
                        ansGraph.get(j).set(i, new Pair<>(0, ""));
                        System.out.println(input[i].name + " pays Rs " + iToJ.getFirst() + " to " + input[j].name + " via " + iToJ.getSecond());
                    } else {
                        ansGraph.get(j).set(i, new Pair<>(jToI.getFirst() - iToJ.getFirst(), jToI.getSecond()));
                        ansGraph.get(i).set(j, new Pair<>(0, ""));
                        System.out.println(input[j].name + " pays Rs " + jToI.getFirst() + " to " + input[i].name + " via " + jToI.getSecond());
                    }
                } else if (iToJ.getFirst() != 0) {
                    System.out.println(input[i].name + " pays Rs " + iToJ.getFirst() + " to " + input[j].name + " via " + iToJ.getSecond());
                } else if (jToI.getFirst() != 0) {
                    System.out.println(input[j].name + " pays Rs " + jToI.getFirst() + " to " + input[i].name + " via " + jToI.getSecond());
                }

                ansGraph.get(i).set(j, new Pair<>(0, ""));
                ansGraph.get(j).set(i, new Pair<>(0, ""));
            }
        }
        System.out.println();
    }

    public static void minimizeCashFlow(int numBanks, Bank[] input, Map<String, Integer> indexOf, int numTransactions, int[][] graph, int maxNumTypes) {
        Bank[] listOfNetAmounts = new Bank[numBanks];
        for (int b = 0; b < numBanks; b++) {
            listOfNetAmounts[b] = new Bank();
            listOfNetAmounts[b].name = input[b].name;
            listOfNetAmounts[b].types = input[b].types;

            int amount = 0;
            for (int i = 0; i < numBanks; i++) {
                amount += graph[i][b];
            }
            for (int j = 0; j < numBanks; j++) {
                amount -= graph[b][j];
            }
            listOfNetAmounts[b].netAmount = amount;
        }

        List<List<Pair<Integer, String>>> ansGraph = new ArrayList<>(numBanks);
        for (int i = 0; i < numBanks; i++) {
            ansGraph.add(new ArrayList<>(Collections.nCopies(numBanks, new Pair<>(0, ""))));
        }

        int numZeroNetAmounts = 0;
        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) numZeroNetAmounts++;
        }

        while (numZeroNetAmounts != numBanks) {
            int minIndex = getMinIndex(listOfNetAmounts, numBanks);
            Pair<Integer, String> maxAns = getMaxIndex(listOfNetAmounts, numBanks, minIndex, input, maxNumTypes);
            int maxIndex = maxAns.getFirst();

            if (maxIndex == -1) {
                ansGraph.get(minIndex).set(0, new Pair<>(Math.abs(listOfNetAmounts[minIndex].netAmount), input[minIndex].types.iterator().next()));
                int simpleMaxIndex = getSimpleMaxIndex(listOfNetAmounts, numBanks);
                ansGraph.get(0).set(simpleMaxIndex, new Pair<>(Math.abs(listOfNetAmounts[minIndex].netAmount), input[simpleMaxIndex].types.iterator().next()));

                listOfNetAmounts[simpleMaxIndex].netAmount += listOfNetAmounts[minIndex].netAmount;
                listOfNetAmounts[minIndex].netAmount = 0;

                if (listOfNetAmounts[minIndex].netAmount == 0) numZeroNetAmounts++;
                if (listOfNetAmounts[simpleMaxIndex].netAmount == 0) numZeroNetAmounts++;
            } else {
                int transactionAmount = Math.min(Math.abs(listOfNetAmounts[minIndex].netAmount), listOfNetAmounts[maxIndex].netAmount);
                ansGraph.get(minIndex).set(maxIndex, new Pair<>(transactionAmount, maxAns.getSecond()));

                listOfNetAmounts[minIndex].netAmount += transactionAmount;
                listOfNetAmounts[maxIndex].netAmount -= transactionAmount;

                if (listOfNetAmounts[minIndex].netAmount == 0) numZeroNetAmounts++;
                if (listOfNetAmounts[maxIndex].netAmount == 0) numZeroNetAmounts++;
            }
        }

        printAns(ansGraph, numBanks, input);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("\n********************* Welcome to CASH FLOW MINIMIZER SYSTEM ***********************\n");
        System.out.println("This system minimizes the number of transactions among multiple banks in different corners of the world that use different modes of payment.\n");

        System.out.println("Enter the number of banks participating in the transactions.");
        int numBanks = sc.nextInt();
        Bank[] input = new Bank[numBanks];
        Map<String, Integer> indexOf = new HashMap<>();

        System.out.println("Enter the details of the banks and transactions as stated:");
        System.out.println("Bank name, number of payment modes it has, and the payment modes.");

        int maxNumTypes = 0;
        for (int i = 0; i < numBanks; i++) {
            input[i] = new Bank();
            System.out.print(i == 0 ? "World Bank: " : "Bank " + i + ": ");
            input[i].name = sc.next();
            indexOf.put(input[i].name, i);

            int numTypes = sc.nextInt();
            if (i == 0) maxNumTypes = numTypes;

            for (int j = 0; j < numTypes; j++) {
                input[i].types.add(sc.next());
            }
        }

        System.out.println("Enter the number of transactions.");
        int numTransactions = sc.nextInt();
        int[][] graph = new int[numBanks][numBanks];

        System.out.println("Enter the details of each transaction as stated below:");
        System.out.println("From, to, amount.");
        for (int i = 0; i < numTransactions; i++) {
            String fromBank = sc.next();
            String toBank = sc.next();
            int amount = sc.nextInt();

            int from = indexOf.get(fromBank);
            int to = indexOf.get(toBank);

            graph[from][to] = amount;
        }

        minimizeCashFlow(numBanks, input, indexOf, numTransactions, graph, maxNumTypes);

        sc.close();
    }
}

// Helper class to hold pairs of values
class Pair<K, V> {
    private final K first;
    private final V second;

    public Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }
}
