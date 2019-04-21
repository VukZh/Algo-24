package rleapp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;


public class RLEApp {

    public static Byte[] ReadFile(String filepath) { // чтение в Byte[] (Byte используются для обработки в RLEPack и RLEUnPack)
        try (FileInputStream inFile = new FileInputStream(filepath)) {
            byte[] readbyte = new byte[inFile.available()];
            inFile.read(readbyte);
            Byte[] res = new Byte[readbyte.length];
            int i = 0;
            for (byte x : readbyte) {
                res[i++] = x;
            }
            return res;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public static void WriteFile(Byte[] out, String filepath) { // запись в byte[]
        try (FileOutputStream outFile = new FileOutputStream(filepath)) {
            byte[] writebyte = new byte[out.length];
            int i = 0;
            for (Byte x : out) {
                writebyte[i++] = x;
            }
            outFile.write(writebyte, 0, writebyte.length);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    Byte[] RLEPack(Byte[] inByte) { // простая упаковка RLE (ArrayList <Byte> для временного хранения)
        ArrayList<Byte> bytePack = new ArrayList<>();
        Byte[] res;
        int len = inByte.length;
        for (int i = 0; i < len; i++) {
            int counter = 1;
            while (i < len - 1 && Objects.equals(inByte[i], inByte[i + 1]) && counter % 254 != 0) {
                i++;
                counter++;
            }
            bytePack.add((byte) (counter - 127)); // меняем диапазон для byte 0..254
            bytePack.add(inByte[i]);
        }
        res = new Byte[bytePack.size()];
        int i = 0;
        for (Byte x : bytePack) {
            res[i++] = x;
        }
        return res;
    }

    Byte[] RLEPack2(Byte[] inByte) { // улучшенная упаковка RLE (ArrayList <Byte> для временного хранения)
        ArrayList<Byte> bytePack = new ArrayList<>();
        Byte[] res;
        int len = inByte.length;
        int start1 = -1, end1 = -1;
        for (int i = 0; i < len; i++) {
            int counter = 1;
            boolean flagNo1 = false;
            boolean flagNo2 = false;
            if (i < len - 1 && Objects.equals(inByte[i], inByte[i + 1])) {
                flagNo2 = true;
            }
            while (i < len - 1 && Objects.equals(inByte[i], inByte[i + 1]) && counter % 127 != 0) { // считаем одинаковые соседние байты
                i++;
                counter++;
                flagNo1 = true;
            }
            if (counter == 1 && !flagNo1) { // считаем одиночные байты; запоминаем их начало и конец
                flagNo2 = true;
                if (start1 == -1) {
                    start1 = i;
                }
                end1 = i;
            }
            if ((flagNo1 && flagNo2 && start1 != -1) || (i == len - 1 && end1 != start1)) { // одиночные байты закончились 
                while (true) {
                    int sizeArr1 = end1 - start1 + 1;
                    if (sizeArr1 > 128) { // если неповторяющаяся последовательность > 128 заносим массив 128 байт и сдвигаем начало
                        bytePack.add((byte) (-128));
                        for (int j = start1; j < start1 + 128; j++) {
                            bytePack.add(inByte[j]);
                        }
                        start1 = start1 + 128;
                    } else { // если неповторяющаяся последовательность <= 128 заносим весь массив
                        bytePack.add((byte) ((end1 - start1 + 1) * (-1)));
                        for (int j = start1; j <= end1; j++) {
                            bytePack.add(inByte[j]);
                        }
                        break;
                    }
                }
                i = end1;
                start1 = -1; // обновляемся для следующей неповторяющейся последовательности
                end1 = -1;
            } else if (flagNo1) { // сохраняем повторяющиеся последовательности
                bytePack.add((byte) (counter));
                bytePack.add(inByte[i]);
            }
        }

        res = new Byte[bytePack.size()]; // ArrayList -> Byte
        int i = 0;
        for (Byte x : bytePack) {
            res[i++] = x;
        }
        return res;
    }

    Byte[] RLEUnPack(Byte[] inByte) { // простая распаковка RLE(ArrayList <Byte> для временного хранения)
        ArrayList<Byte> byteUnPack = new ArrayList<>();
        Byte[] res;
        int len = inByte.length;
        for (int i = 0; i < len; i = i + 2) {
            int counter = (int) inByte[i] + 127; // меняем диапазон для int 0..254
            while (counter > 0) {
                byteUnPack.add(inByte[i + 1]);
                counter--;
            }
        }
        res = new Byte[byteUnPack.size()];
        int i = 0;
        for (Byte x : byteUnPack) {
            res[i++] = x;
        }
        return res;
    }

    Byte[] RLEUnPack2(Byte[] inByte) { // улучшенная распаковка RLE(ArrayList <Byte> для временного хранения)
        ArrayList<Byte> byteUnPack = new ArrayList<>();
        Byte[] res;
        int len = inByte.length;
        for (int i = 0; i < len;) { // проходим по запакованному массиву, шаг считаем в зависимости от "+" или "-" счетчика
            int counter = (int) inByte[i]; 
            if (counter < 0) { // последовательность различных соседних байт 
                for (int j = 0; j < counter * -1; j++) {
                    byteUnPack.add(inByte[i + 1 + j]);
                }
                i = -1 * counter + i + 1;
            } else { // последовательность одинаковых соседних байт 
                while (counter > 0) {
                    byteUnPack.add(inByte[i + 1]);
                    counter--;

                }
                i = i + 2;
            }         
        }
        res = new Byte[byteUnPack.size()]; // ArrayList -> Byte
        int i = 0;
        for (Byte x : byteUnPack) {
            res[i++] = x;
        }
        return res;
    }

    public static void main(String[] args) {

        RLEApp r1 = new RLEApp(); 
        // тест улучшенного RLE
        Byte[] s1 = ReadFile("src\\rleapp\\1.exe"); // тестируем exe
        Byte[] s2 = r1.RLEPack2(s1);
        WriteFile(s2, "src\\rleapp\\1p.exe");
        Byte[] s3 = r1.RLEUnPack2(s2);
        WriteFile(s3, "src\\rleapp\\1u.exe");
        System.out.println("exe OK");
        s1 = ReadFile("src\\rleapp\\1.jpg"); // тестируем jpg
        s2 = r1.RLEPack2(s1);
        WriteFile(s2, "src\\rleapp\\1p.jpg");
        s3 = r1.RLEUnPack2(s2);
        WriteFile(s3, "src\\rleapp\\1u.jpg");
        System.out.println("jpg OK");
        s1 = ReadFile("src\\rleapp\\1.txt"); // тестируем txt
        s2 = r1.RLEPack2(s1);
        WriteFile(s2, "src\\rleapp\\1p.txt");
        s3 = r1.RLEUnPack2(s2);
        WriteFile(s3, "src\\rleapp\\1u.txt");
        System.out.println("txt OK");
        
        // тест простого RLE
        s1 = ReadFile("src\\rleapp\\1.exe"); // тестируем exe
        s2 = r1.RLEPack(s1);
        WriteFile(s2, "src\\rleapp\\2p.exe");
        s3 = r1.RLEUnPack(s2);
        WriteFile(s3, "src\\rleapp\\2u.exe");
        System.out.println("exe OK");
        s1 = ReadFile("src\\rleapp\\1.jpg"); // тестируем jpg
        s2 = r1.RLEPack(s1);
        WriteFile(s2, "src\\rleapp\\2p.jpg");
        s3 = r1.RLEUnPack(s2);
        WriteFile(s3, "src\\rleapp\\2u.jpg");
        System.out.println("jpg OK");
        s1 = ReadFile("src\\rleapp\\1.txt"); // тестируем txt
        s2 = r1.RLEPack(s1);
        WriteFile(s2, "src\\rleapp\\2p.txt");
        s3 = r1.RLEUnPack(s2);
        WriteFile(s3, "src\\rleapp\\2u.txt");
        System.out.println("txt OK");
        

    }

}
