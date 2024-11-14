package com.example.centus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebtMinimization {

    // Klasa reprezentująca pojedynczą transakcję między użytkownikami
    public static class Transaction {
        public String fromUser;
        public String toUser;
        public int amount;

        public Transaction(String fromUser, String toUser, int amount) {
            this.fromUser = fromUser;
            this.toUser = toUser;
            this.amount = amount;
        }

        @Override
        public String toString() {
            return fromUser + " płaci " + toUser + " " + amount + " zł";
        }
    }

    // Funkcja do obliczenia sald dla każdego użytkownika
    public static Map<String, Integer> calculateBalances(Map<String, Map<String, Integer>> debts) {
        Map<String, Integer> balances = new HashMap<>();

        // Przeliczamy salda na podstawie długów
        for (String user : debts.keySet()) {
            for (Map.Entry<String, Integer> entry : debts.get(user).entrySet()) {
                String targetUser = entry.getKey();
                int amount = entry.getValue();

                // Odejmujemy od salda płacącego
                balances.put(user, balances.getOrDefault(user, 0) - amount);
                // Dodajemy do salda odbiorcy
                balances.put(targetUser, balances.getOrDefault(targetUser, 0) + amount);
            }
        }
        return balances;
    }

    // Funkcja do minimalizacji przepływów pieniężnych
    public static List<Transaction> minimizeTransactions(Map<String, Integer> balances) {
        List<Transaction> transactions = new ArrayList<>();

        // Tworzymy listy użytkowników z dodatnimi i ujemnymi saldami
        List<Map.Entry<String, Integer>> debtors = new ArrayList<>();
        List<Map.Entry<String, Integer>> creditors = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : balances.entrySet()) {
            if (entry.getValue() < 0) {
                debtors.add(entry);  // Użytkownicy mający saldo ujemne
            } else if (entry.getValue() > 0) {
                creditors.add(entry);  // Użytkownicy mający saldo dodatnie
            }
        }

        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            Map.Entry<String, Integer> debtor = debtors.get(i);
            Map.Entry<String, Integer> creditor = creditors.get(j);

            int debtAmount = Math.min(-debtor.getValue(), creditor.getValue());

            // Tworzymy transakcję, aby zmniejszyć saldo obu użytkowników
            transactions.add(new Transaction(debtor.getKey(), creditor.getKey(), debtAmount));

            // Aktualizujemy saldo po transakcji
            debtor.setValue(debtor.getValue() + debtAmount);
            creditor.setValue(creditor.getValue() - debtAmount);

            // Przechodzimy do następnych użytkowników o saldach różnym od 0
            if (debtor.getValue() == 0) i++;
            if (creditor.getValue() == 0) j++;
        }

        return transactions;
    }
}

