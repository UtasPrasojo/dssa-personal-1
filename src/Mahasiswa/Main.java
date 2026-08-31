package Mahasiswa;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        Mahasiswa[] daftar = new Mahasiswa[5];
        daftar[0] = new Mahasiswa("Andi Pratama", "2440001", "Teknik Informatika", 3.75);
        daftar[1] = new Mahasiswa("Budi Santoso", "2440002", "Sistem Informasi", 3.40);
        daftar[2] = new Mahasiswa("Citra Lestari", "2440003", "Teknik Informatika", 3.90);
        daftar[3] = new Mahasiswa("Joni Suhartono", "2440004", "Teknik Industri", 3.00);
        daftar[4] = new Mahasiswa("Bulan Suci", "2440005", "Akuntansi", 3.20);


        System.out.println(">>> DATA MAHASISWA AWAL <<<\n");
        for (Mahasiswa m : daftar) {
            m.tampilkanInfo();
        }


        System.out.print("Masukkan NIM mahasiswa yang ingin diupdate: ");
        String nimInput = scanner.nextLine();

        System.out.print("Masukkan IPK baru: ");
        double ipkBaru = Double.parseDouble(scanner.nextLine());

        boolean ditemukan = false;
        for (Mahasiswa m : daftar) {
            if (m.getNim().equals(nimInput)) {
                m.updateIpk(ipkBaru);
                ditemukan = true;
                System.out.println("Data berhasil diperbarui!\n");
                m.tampilkanInfo(); 
                break;
            }
        }

        if (!ditemukan) {
            System.out.println("NIM tidak ditemukan!");
        }

        scanner.close();
    }
}