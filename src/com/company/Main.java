package com.company;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    public static void main(String[] args) {
        final Account a = new Account(1000);
        final Account b = new Account(2000);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    transfer(a, b, 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    transfer(b, a, 300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    static void transfer(Account acc1, Account acc2, int amount) throws InterruptedException {

        /** При такой синхронизации происходит взаимная блокировка потоков (deadlock),
         * т.к. не определен порядок взятия аккаунта */
//        synchronized (acc1) {
//            Thread.sleep(1000);
//            synchronized (acc2) {
//                if (acc1.getBalance() < amount) {
//                    System.out.println("недостаточно средств для операции");
//                }
//                acc1.withdraw(amount);
//                acc2.deposit(amount);
//            }
//        }

        /** сначала попытаемся залочить 1-й аккаунт, потом 2-й,
         * время на попытку указано в скобках, по истечению времени лок спадает,
         * операция после локов должна занимать явно меньше времени чем длятся локи для того чтобы успеть ее совершить */
        if (acc1.getLock().tryLock(1000, TimeUnit.MILLISECONDS)) {
            try {
                if (acc2.getLock().tryLock(1000, TimeUnit.MILLISECONDS)) {
                    try {
                if (acc1.getBalance() < amount) {
                    System.out.println("недостаточно средств для операции");
                } else {
                    acc1.withdraw(amount);
                    acc2.deposit(amount);
                }
                    } finally {

                    }
                }
            } finally {
                acc1.getLock().unlock();
            }
        }
    }
}

class Account {
    private int balance;

    Lock lock = new ReentrantLock();

    public Account(int balance) {
        this.balance = balance;
    }

    public int getBalance() {
        return balance;
    }

    public Lock getLock() {
        return lock;
    }

    public void withdraw(int amount) {
        balance -= amount;
        System.out.println("C баланса произведено списание на " + amount + ", остаток составляет " + balance);
    }

    public void deposit(int amount) {
        balance += amount;
        System.out.println("Баланс пополнен на " + amount + ", и составляет " + balance);
    }
}
