package final_project;

import java.io.*;
import java.text.DecimalFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * FinalProject.java
 * Kasir OOP lengkap:
 * - Multi-user (Kasir/Admin/Owner) dengan PIN
 * - Admin: CRUD menu (menu_master.txt)
 * - Kasir: transaksi multi-item, struk, simpan ke sales_log.txt
 * - Owner: total pemasukan, top 5 produk terlaris, laporan harian (7 hari terakhir)
 *
 * Data files (created next to jar/class):
 * - menu_master.txt   -> menyimpan master menu, format: id|nama|harga|kategori
 * - sales_log.txt     -> menyimpan setiap transaksi: yyyy-MM-dd HH:mm:ss|nama|qty|subtotal
 *
 * PIN default:
 * - Kasir  : 1111
 * - Admin  : 2222
 * - Owner  : 3333
 *
 * Author: ChatGPT for Ilham
 * Tanggal: 2025-12-05
 */

class MenuItem {
    int id;
    String name;
    int price;
    String category;

    public MenuItem(int id, String name, int price, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }
}

class OrderLine {
    MenuItem item;
    int qty;

    public OrderLine(MenuItem item, int qty) {
        this.item = item;
        this.qty = qty;
    }

    public int subtotal() {
        return item.price * qty;
    }
}

class MasterMenu {
    private List<MenuItem> items = new ArrayList<>();
    private final File file = new File("menu_master.txt");
    private DecimalFormat fmt = new DecimalFormat("#,###");

    public MasterMenu() {
        loadFromFile();
        if (items.isEmpty()) {
            loadDefaultMenu();
            saveToFile();
        }
    }

    private void loadDefaultMenu() {
        // defaults similar to versi awal
        addItem(new MenuItem(nextId(), "Ayam Geprek (Pakai Nasi)", 10000, "Makanan"));
        addItem(new MenuItem(nextId(), "Ayam Geprek (Tanpa Nasi)", 8000, "Makanan"));
        addItem(new MenuItem(nextId(), "Ayam Rica (Pakai Nasi)", 10000, "Makanan"));
        addItem(new MenuItem(nextId(), "Ayam Rica (Tanpa Nasi)", 7000, "Makanan"));
        addItem(new MenuItem(nextId(), "Tempe Tepung (pcs)", 2000, "Makanan"));
        addItem(new MenuItem(nextId(), "Es Teh Kecil", 2000, "Minuman"));
        addItem(new MenuItem(nextId(), "Es Teh Besar", 3000, "Minuman"));
        addItem(new MenuItem(nextId(), "Es Jeruk Kecil", 3000, "Minuman"));
        addItem(new MenuItem(nextId(), "Es Jeruk Besar", 4000, "Minuman"));
        addItem(new MenuItem(nextId(), "Es Capucino Kecil", 4000, "Minuman"));
        addItem(new MenuItem(nextId(), "Es Capucino Besar", 5000, "Minuman"));
        addItem(new MenuItem(nextId(), "Cah Kangkung", 5000, "Sayur"));
        addItem(new MenuItem(nextId(), "Oseng Buncis", 5000, "Sayur"));
        addItem(new MenuItem(nextId(), "Sayur Asem", 5000, "Sayur"));
        addItem(new MenuItem(nextId(), "Penyetan Terong", 5000, "Sayur"));
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public void loadFromFile() {
        items.clear();
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // format: id|nama|harga|kategori
                String[] p = line.split("\\|", 4);
                if (p.length < 4) continue;
                int id = Integer.parseInt(p[0]);
                String name = p[1];
                int price = Integer.parseInt(p[2]);
                String cat = p[3];
                items.add(new MenuItem(id, name, price, cat));
            }
        } catch (Exception e) {
            System.out.println("Gagal membaca menu_master.txt : " + e.getMessage());
        }
    }

    public void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (MenuItem it : items) {
                bw.write(it.id + "|" + it.name + "|" + it.price + "|" + it.category);
                bw.newLine();
            }
        } catch (Exception e) {
            System.out.println("Gagal menyimpan menu_master.txt : " + e.getMessage());
        }
    }

    public int nextId() {
        int max = 0;
        for (MenuItem it : items) if (it.id > max) max = it.id;
        return max + 1;
    }

    public void addItem(MenuItem it) {
        items.add(it);
    }

    public MenuItem findById(int id) {
        for (MenuItem it : items) if (it.id == id) return it;
        return null;
    }

    public List<MenuItem> searchByName(String q) {
        q = q.toLowerCase();
        List<MenuItem> res = new ArrayList<>();
        for (MenuItem it : items) if (it.name.toLowerCase().contains(q)) res.add(it);
        return res;
    }

    public void deleteById(int id) {
        items.removeIf(it -> it.id == id);
    }

    public void printAll() {
        System.out.println("\n===== DAFTAR MENU =====");
        System.out.printf("%-4s %-30s %-10s %-10s\n", "ID", "NAMA", "HARGA", "KATEGORI");
        for (MenuItem it : items) {
            System.out.printf("%-4d %-30s Rp.%-10s %-10s\n", it.id, it.name, fmt.format(it.price), it.category);
        }
    }
}

class SalesLogger {
    private final File file = new File("sales_log.txt");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // menyimpan tiap baris transaksi: timestamp|itemName|qty|subtotal
    public void logOrderLine(OrderLine ol) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            String ts = LocalDateTime.now().format(dtf);
            bw.write(ts + "|" + ol.item.name + "|" + ol.qty + "|" + ol.subtotal());
            bw.newLine();
        } catch (Exception e) {
            System.out.println("Gagal menulis ke sales_log.txt : " + e.getMessage());
        }
    }

    // menyimpan seluruh transaksi (dipakai saat cetak struk -> juga simpan per baris)
    public void logTransaction(List<OrderLine> lines) {
        for (OrderLine ol : lines) logOrderLine(ol);
    }

    // membaca log; mengembalikan list baris yang ter-parse
    public List<String[]> readAll() {
        List<String[]> rows = new ArrayList<>();
        if (!file.exists()) return rows;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // ts|name|qty|subtotal
                String[] p = line.split("\\|", 4);
                if (p.length == 4) rows.add(p);
            }
        } catch (Exception e) {
            System.out.println("Gagal membaca sales_log.txt : " + e.getMessage());
        }
        return rows;
    }
}

class Reporter {
    private SalesLogger logger;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private DecimalFormat fmt = new DecimalFormat("#,###");

    public Reporter(SalesLogger l) {
        this.logger = l;
    }

    // total pemasukan seluruh log (bisa diberi filter tanggal)
    public int totalIncome(Optional<LocalDate> from, Optional<LocalDate> to) {
        int total = 0;
        List<String[]> rows = logger.readAll();
        for (String[] r : rows) {
            LocalDateTime ts = LocalDateTime.parse(r[0], dtf);
            LocalDate d = ts.toLocalDate();
            if (from.isPresent() && d.isBefore(from.get())) continue;
            if (to.isPresent() && d.isAfter(to.get())) continue;
            int subtotal = Integer.parseInt(r[3]);
            total += subtotal;
        }
        return total;
    }

    // top N best selling by qty (optional date filter)
    public List<Map.Entry<String, Integer>> topProducts(int topN, Optional<LocalDate> from, Optional<LocalDate> to) {
        Map<String, Integer> agg = new HashMap<>();
        List<String[]> rows = logger.readAll();
        for (String[] r : rows) {
            LocalDateTime ts = LocalDateTime.parse(r[0], dtf);
            LocalDate d = ts.toLocalDate();
            if (from.isPresent() && d.isBefore(from.get())) continue;
            if (to.isPresent() && d.isAfter(to.get())) continue;
            String name = r[1];
            int qty = Integer.parseInt(r[2]);
            agg.put(name, agg.getOrDefault(name, 0) + qty);
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(agg.entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue())); // descending
        if (list.size() > topN) return list.subList(0, topN);
        return list;
    }

    // print laporan harian selama N hari (kembali dari hari ini)
    public void printDailyReport(int daysBack) {
        LocalDate today = LocalDate.now();
        System.out.println("\n=== Laporan Harian (7 hari terakhir) ===");
        for (int i = 0; i < daysBack; i++) {
            LocalDate d = today.minusDays(i);
            int total = totalIncome(Optional.of(d), Optional.of(d));
            System.out.println(d + " : Rp." + fmt.format(total));
        }
    }
}

public class Final_Project {
    private static Scanner in = new Scanner(System.in);
    private static MasterMenu master = new MasterMenu();
    private static SalesLogger logger = new SalesLogger();
    private static Reporter reporter = new Reporter(logger);
    private static DecimalFormat fmt = new DecimalFormat("#,###");

    // PIN constants
    private static final String PIN_KASIR = "1111";
    private static final String PIN_ADMIN = "2222";
    private static final String PIN_OWNER = "3333";

    public static void main(String[] args) {
        System.out.println("=== SELAMAT DATANG DI KASIR DEPOT MBAK RARA (OOP MULTIUSER) ===");
        while (true) {
            System.out.println("\nPilih mode:");
            System.out.println("1. Login (masukkan PIN)");
            System.out.println("2. Keluar");
            System.out.print("Pilihan: ");
            int pilihan = validInt();
            if (pilihan == 1) login();
            else {
                System.out.println("Terima kasih. Sampai jumpa!");
                break;
            }
        }
    }

    private static void login() {
        System.out.print("\nMasukkan PIN: ");
        String pin = in.next();
        if (pin.equals(PIN_KASIR)) {
            kasirMenu();
        } else if (pin.equals(PIN_ADMIN)) {
            adminMenu();
        } else if (pin.equals(PIN_OWNER)) {
            ownerMenu();
        } else {
            System.out.println("PIN salah. Kembali ke menu utama.");
        }
    }

    // ================= KASIR =================
    private static void kasirMenu() {
        System.out.println("\n-- MODE: KASIR --");
        List<OrderLine> cart = new ArrayList<>();
        while (true) {
            System.out.println("\n1. Tambah item ke keranjang");
            System.out.println("2. Tampilkan keranjang");
            System.out.println("3. Ubah jumlah item di keranjang");
            System.out.println("4. Hapus item dari keranjang");
            System.out.println("5. Cetak struk & Selesai Transaksi");
            System.out.println("6. Batal & Kembali");
            System.out.print("Pilihan: ");
            int p = validInt();
            switch (p) {
                case 1 -> addItemToCart(cart);
                case 2 -> printCart(cart);
                case 3 -> editCart(cart);
                case 4 -> removeFromCart(cart);
                case 5 -> {
                    if (cart.isEmpty()) {
                        System.out.println("Keranjang kosong, tidak ada yang dicetak.");
                    } else {
                        checkoutAndPrint(cart);
                        cart.clear();
                    }
                }
                case 6 -> {
                    System.out.println("Kembali ke menu utama.");
                    return;
                }
                default -> System.out.println("Pilihan tidak valid.");
            }
        }
    }

    private static void addItemToCart(List<OrderLine> cart) {
        master.printAll();
        System.out.print("\nMasukkan ID menu yang ingin ditambah: ");
        int id = validInt();
        MenuItem mi = master.findById(id);
        if (mi == null) {
            System.out.println("Menu dengan ID tersebut tidak ditemukan.");
            return;
        }
        System.out.print("Masukkan jumlah: ");
        int qty = validInt();
        if (qty <= 0) {
            System.out.println("Jumlah harus > 0");
            return;
        }
        // jika item sama, gabungkan qty
        boolean found = false;
        for (OrderLine ol : cart) {
            if (ol.item.id == mi.id) {
                ol.qty += qty;
                found = true;
                break;
            }
        }
        if (!found) cart.add(new OrderLine(mi, qty));
        System.out.println("Berhasil ditambahkan ke keranjang.");
    }

    private static void printCart(List<OrderLine> cart) {
        System.out.println("\n=== KERANJANG ===");
        if (cart.isEmpty()) {
            System.out.println("Keranjang kosong.");
            return;
        }
        int i = 1;
        int total = 0;
        for (OrderLine ol : cart) {
            System.out.printf("%d. %s x%d -> Rp.%s\n", i, ol.item.name, ol.qty, fmt.format(ol.subtotal()));
            total += ol.subtotal();
            i++;
        }
        System.out.println("---------------------------");
        System.out.println("TOTAL : Rp." + fmt.format(total));
    }

    private static void editCart(List<OrderLine> cart) {
        if (cart.isEmpty()) {
            System.out.println("Keranjang kosong.");
            return;
        }
        printCart(cart);
        System.out.print("Pilih nomor item yang ingin diubah: ");
        int idx = validInt();
        if (idx < 1 || idx > cart.size()) {
            System.out.println("Nomor tidak valid.");
            return;
        }
        System.out.print("Masukkan jumlah baru: ");
        int newQty = validInt();
        if (newQty <= 0) {
            System.out.println("Jumlah harus > 0.");
            return;
        }
        cart.get(idx - 1).qty = newQty;
        System.out.println("Jumlah berhasil diubah.");
    }

    private static void removeFromCart(List<OrderLine> cart) {
        if (cart.isEmpty()) {
            System.out.println("Keranjang kosong.");
            return;
        }
        printCart(cart);
        System.out.print("Pilih nomor item yang ingin dihapus: ");
        int idx = validInt();
        if (idx < 1 || idx > cart.size()) {
            System.out.println("Nomor tidak valid.");
            return;
        }
        cart.remove(idx - 1);
        System.out.println("Item terhapus dari keranjang.");
    }

    private static void checkoutAndPrint(List<OrderLine> cart) {
        System.out.println("\n===== STRUK PEMBELIAN =====");
        int total = 0;
        for (OrderLine ol : cart) {
            System.out.printf("%s x%d -> Rp.%s\n", ol.item.name, ol.qty, fmt.format(ol.subtotal()));
            total += ol.subtotal();
        }
        int diskon = hitungDiskon(total);
        int bayarAkhir = total - diskon;
        System.out.println("--------------------------");
        System.out.println("Subtotal : Rp." + fmt.format(total));
        System.out.println("Diskon   : Rp." + fmt.format(diskon));
        System.out.println("TOTAL    : Rp." + fmt.format(bayarAkhir));
        System.out.print("Uang tunai : Rp.");
        int cash = validInt();
        while (cash < bayarAkhir) {
            System.out.print("Uang kurang. Masukkan jumlah yang benar: Rp.");
            cash = validInt();
        }
        System.out.println("KEMBALIAN : Rp." + fmt.format(cash - bayarAkhir));
        System.out.println("Terima kasih. Transaksi selesai.");
        // log tiap orderline
        logger.logTransaction(convertToOrderLines(cart));
    }

    // diskon: contoh 10% jika >= 50000
    private static int hitungDiskon(int total) {
        if (total >= 50000) return (int) (total * 0.10);
        return 0;
    }

    private static List<OrderLine> convertToOrderLines(List<OrderLine> cart) {
        return new ArrayList<>(cart);
    }

    // ================= ADMIN =================
    private static void adminMenu() {
        System.out.println("\n-- MODE: ADMIN --");
        while (true) {
            System.out.println("\n1. Tampilkan semua menu");
            System.out.println("2. Tambah menu baru");
            System.out.println("3. Ubah menu (cari dulu)");
            System.out.println("4. Hapus menu");
            System.out.println("5. Kembali");
            System.out.print("Pilihan: ");
            int p = validInt();
            switch (p) {
                case 1 -> master.printAll();
                case 2 -> adminAddMenu();
                case 3 -> adminEditMenu();
                case 4 -> adminDeleteMenu();
                case 5 -> {
                    master.saveToFile();
                    System.out.println("Perubahan disimpan. Kembali ke menu utama.");
                    return;
                }
                default -> System.out.println("Pilihan tidak valid.");
            }
        }
    }

    private static void adminAddMenu() {
        System.out.print("Nama menu: ");
        String nama = readLineTrim();
        System.out.print("Harga (integer): ");
        int harga = validInt();
        System.out.print("Kategori: ");
        String cat = readLineTrim();
        MenuItem it = new MenuItem(master.nextId(), nama, harga, cat);
        master.addItem(it);
        master.saveToFile();
        System.out.println("Menu berhasil ditambahkan.");
    }

    private static void adminEditMenu() {
        System.out.print("Cari dengan nama (bisa partial): ");
        String q = readLineTrim();
        List<MenuItem> res = master.searchByName(q);
        if (res.isEmpty()) {
            System.out.println("Tidak ditemukan.");
            return;
        }
        System.out.println("Hasil pencarian:");
        for (MenuItem it : res) {
            System.out.printf("%d. %s - Rp.%s [%s]\n", it.id, it.name, fmt.format(it.price), it.category);
        }
        System.out.print("Masukkan ID menu yang ingin diubah: ");
        int id = validInt();
        MenuItem select = master.findById(id);
        if (select == null) {
            System.out.println("ID tidak ada.");
            return;
        }
        System.out.println("Edit (biarkan kosong jika tidak diubah)");
        System.out.print("Nama baru (" + select.name + "): ");
        String newName = readLineAllowEmpty();
        if (!newName.isEmpty()) select.name = newName;
        System.out.print("Harga baru (" + select.price + "): ");
        String hargaStr = readLineAllowEmpty();
        if (!hargaStr.isEmpty()) {
            try {
                select.price = Integer.parseInt(hargaStr);
            } catch (Exception e) {
                System.out.println("Format harga salah, tidak diubah.");
            }
        }
        System.out.print("Kategori baru (" + select.category + "): ");
        String newCat = readLineAllowEmpty();
        if (!newCat.isEmpty()) select.category = newCat;
        master.saveToFile();
        System.out.println("Menu berhasil diperbarui.");
    }

    private static void adminDeleteMenu() {
        master.printAll();
        System.out.print("Masukkan ID menu yang ingin dihapus: ");
        int id = validInt();
        MenuItem it = master.findById(id);
        if (it == null) {
            System.out.println("ID tidak ditemukan.");
            return;
        }
        System.out.print("Yakin hapus " + it.name + " ? (y/n): ");
        String c = in.next();
        if (c.equalsIgnoreCase("y")) {
            master.deleteById(id);
            master.saveToFile();
            System.out.println("Terhapus.");
        } else System.out.println("Batal.");
    }

    // ================= OWNER =================
    private static void ownerMenu() {
        System.out.println("\n-- MODE: OWNER --");
        while (true) {
            System.out.println("\n1. Lihat total pemasukan (seluruh waktu)");
            System.out.println("2. Lihat total pemasukan (hari tertentu / rentang)");
            System.out.println("3. Top 5 produk terlaris (seluruh waktu)");
            System.out.println("4. Top 5 produk terlaris (7 hari terakhir)");
            System.out.println("5. Laporan harian (7 hari terakhir)");
            System.out.println("6. Kembali");
            System.out.print("Pilihan: ");
            int p = validInt();
            switch (p) {
                case 1 -> {
                    int total = reporter.totalIncome(Optional.empty(), Optional.empty());
                    System.out.println("Total pemasukan seluruh waktu: Rp." + fmt.format(total));
                }
                case 2 -> ownerTotalByRange();
                case 3 -> printTopN(5, Optional.empty(), Optional.empty());
                case 4 -> {
                    LocalDate now = LocalDate.now();
                    LocalDate from = now.minusDays(6); // 7 hari termasuk hari ini
                    printTopN(5, Optional.of(from), Optional.of(now));
                }
                case 5 -> reporter.printDailyReport(7);
                case 6 -> {
                    System.out.println("Kembali ke menu utama.");
                    return;
                }
                default -> System.out.println("Pilihan tidak valid.");
            }
        }
    }

    private static void ownerTotalByRange() {
        System.out.println("Masukkan tanggal awal (yyyy-mm-dd) atau kosong untuk semua: ");
        String a = readLineAllowEmpty();
        System.out.println("Masukkan tanggal akhir (yyyy-mm-dd) atau kosong untuk semua: ");
        String b = readLineAllowEmpty();
        Optional<LocalDate> from = Optional.empty();
        Optional<LocalDate> to = Optional.empty();
        try {
            if (!a.isEmpty()) from = Optional.of(LocalDate.parse(a));
            if (!b.isEmpty()) to = Optional.of(LocalDate.parse(b));
            int total = reporter.totalIncome(from, to);
            System.out.println("Total pemasukan: Rp." + fmt.format(total));
        } catch (Exception e) {
            System.out.println("Format tanggal salah.");
        }
    }

    private static void printTopN(int n, Optional<LocalDate> from, Optional<LocalDate> to) {
        List<Map.Entry<String, Integer>> top = reporter.topProducts(n, from, to);
        System.out.println("\n--- Top " + n + " Produk Terlaris ---");
        if (top.isEmpty()) System.out.println("Belum ada data penjualan.");
        int rank = 1;
        for (Map.Entry<String, Integer> e : top) {
            System.out.printf("%d. %s -> %d pcs\n", rank, e.getKey(), e.getValue());
            rank++;
        }
    }

    // ================= UTIL =================
    private static int validInt() {
        while (!in.hasNextInt()) {
            System.out.print("Input tidak valid. Masukkan angka: ");
            in.next();
        }
        return in.nextInt();
    }

    // read leftover newline
    private static String readLineTrim() {
        try {
            in.nextLine(); // flush
            return in.nextLine().trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    private static String readLineAllowEmpty() {
        try {
            String s = in.nextLine();
            if (s == null) return "";
            return s.trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }
}
