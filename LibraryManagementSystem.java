import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * A Serializable class to represent a book, replacing the 'struct books' from the C code.
 */
class Book implements Serializable {
    private static final long serialVersionUID = 1L; // Recommended for Serializable classes

    private int id;
    private String name;
    private String author;
    private int quantity;
    private double price;
    private int rackNo;
    private String category;

    // Fields for issued books
    private String studentName;
    private LocalDate issuedDate;
    private LocalDate dueDate;

    public Book(int id, String name, String author, int quantity, double price, int rackNo, String category) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.quantity = quantity;
        this.price = price;
        this.rackNo = rackNo;
        this.category = category;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getAuthor() { return author; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public int getRackNo() { return rackNo; }
    public String getCategory() { return category; }
    public String getStudentName() { return studentName; }
    public LocalDate getIssuedDate() { return issuedDate; }
    public LocalDate getDueDate() { return dueDate; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setAuthor(String author) { this.author = author; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(double price) { this.price = price; }
    public void setRackNo(int rackNo) { this.rackNo = rackNo; }

    /**
     * Sets the issue details for the book.
     * @param studentName The name of the student issuing the book.
     * @param returnTimeDays The number of days until the book is due.
     */
    public void issueBook(String studentName, int returnTimeDays) {
        this.studentName = studentName;
        this.issuedDate = LocalDate.now();
        this.dueDate = this.issuedDate.plusDays(returnTimeDays);
    }
    
    @Override
    public String toString() {
        return String.format("%-15s %-7d %-20s %-20s %-7d %-10.2f %-7d",
                category, id, name, author, quantity, price, rackNo);
    }

    public String toIssuedString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return String.format("%-20s %-15s %-7d %-20s %-15s %-15s",
                studentName, category, id, name, issuedDate.format(formatter), dueDate.format(formatter));
    }
}

/**
 * Main class for the Library Management System.
 */
public class LibraryManagementSystem {

    private static final String BOOK_FILE = "library_books.dat";
    private static final String ISSUE_FILE = "issued_books.dat";
    private static final String PASSWORD = "code";
    private static final int RETURN_TIME = 15;
    private static final Scanner scanner = new Scanner(System.in);
    private static final String[] CATEGORIES = {"Computer", "Electronics", "Electrical", "Civil", "Mechanical"};

    public static void main(String[] args) {
        if (passwordProtected()) {
            mainMenu();
        }
        scanner.close();
        System.out.println("Application closed.");
    }

    /**
     * Clears the console screen using ANSI escape codes.
     * May not work in all terminals (e.g., standard Windows CMD).
     */
    private static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    /**
     * Displays a password prompt before granting access to the main menu.
     */
    private static boolean passwordProtected() {
        clearConsole();
        System.out.println("*********************************************************");
        System.out.println("*                 Password Protected                    *");
        System.out.println("*********************************************************");
        
        for (int i = 0; i < 3; i++) {
            System.out.print("\nEnter Password: ");
            String input = scanner.nextLine();
            if (PASSWORD.equals(input)) {
                System.out.println("\nPassword match. Press Enter to continue...");
                scanner.nextLine();
                return true;
            } else {
                System.out.println("\nWarning!! Incorrect Password. " + (2 - i) + " attempts left.");
            }
        }
        System.out.println("\nToo many incorrect attempts. Exiting.");
        pressEnterToContinue();
        return false;
    }

    /**
     * Displays the main menu and handles user navigation.
     */
    private static void mainMenu() {
        boolean exit = false;
        while (!exit) {
            clearConsole();
            System.out.println("==================== MAIN MENU ====================");
            System.out.println("1. Add Books");
            System.out.println("2. Delete books");
            System.out.println("3. Search Books");
            System.out.println("4. Issue Books");
            System.out.println("5. View Book list");
            System.out.println("6. Edit Book's Record");
            System.out.println("7. Close Application");
            System.out.println("===================================================");
            printCurrentTime();
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1": addBook(); break;
                case "2": deleteBook(); break;
                case "3": searchBooks(); break;
                case "4": issueMenu(); break;
                case "5": viewBooks(); break;
                case "6": editBooks(); break;
                case "7": exit = true; break;
                default:
                    System.out.println("Wrong Entry! Please enter a valid option.");
                    pressEnterToContinue();
                    break;
            }
        }
    }
    
    /**
     * Adds a new book to the library database file.
     */
    private static void addBook() {
        clearConsole();
        System.out.println("--------------- SELECT CATEGORY ---------------");
        for (int i = 0; i < CATEGORIES.length; i++) {
            System.out.printf("%d. %s%n", i + 1, CATEGORIES[i]);
        }
        System.out.print("Enter your choice: ");
        int categoryChoice = getIntInput();
        if (categoryChoice <= 0 || categoryChoice > CATEGORIES.length) {
            System.out.println("Invalid category choice.");
            pressEnterToContinue();
            return;
        }
        String category = CATEGORIES[categoryChoice - 1];

        System.out.println("\n--------------- Enter Book Information ---------------");
        System.out.print("Book ID: ");
        int id = getIntInput();
        if (findBookById(id, BOOK_FILE) != null) {
            System.out.println("A book with this ID already exists.");
            pressEnterToContinue();
            return;
        }

        System.out.print("Book Name: ");
        String name = scanner.nextLine();
        System.out.print("Author: ");
        String author = scanner.nextLine();
        System.out.print("Quantity: ");
        int quantity = getIntInput();
        System.out.print("Price: ");
        double price = getDoubleInput();
        System.out.print("Rack No: ");
        int rackNo = getIntInput();

        Book newBook = new Book(id, name, author, quantity, price, rackNo, category);
        List<Book> books = readFromFile(BOOK_FILE);
        books.add(newBook);
        writeToFile(BOOK_FILE, books);

        System.out.println("\nThe record is successfully saved.");
        pressEnterToContinue();
    }
    
    /**
     * Deletes a book from the library database file.
     */
    private static void deleteBook() {
        clearConsole();
        System.out.println("--------------- Delete Book ---------------");
        System.out.print("Enter the Book ID to delete: ");
        int id = getIntInput();

        List<Book> books = readFromFile(BOOK_FILE);
        Book bookToDelete = null;
        for (Book book : books) {
            if (book.getId() == id) {
                bookToDelete = book;
                break;
            }
        }

        if (bookToDelete != null) {
            System.out.println("Book found: " + bookToDelete.getName());
            System.out.print("Do you want to delete it? (Y/N): ");
            String confirm = scanner.nextLine();
            if (confirm.equalsIgnoreCase("y")) {
                books.remove(bookToDelete);
                writeToFile(BOOK_FILE, books);
                System.out.println("The record was successfully deleted.");
            } else {
                System.out.println("Deletion cancelled.");
            }
        } else {
            System.out.println("No record found for that ID.");
        }
        pressEnterToContinue();
    }

    /**
     * Searches for books by ID or Name.
     */
    private static void searchBooks() {
        clearConsole();
        System.out.println("--------------- Search Books ---------------");
        System.out.println("1. Search By ID");
        System.out.println("2. Search By Name");
        System.out.print("Enter Your Choice: ");
        String choice = scanner.nextLine();
        
        List<Book> books = readFromFile(BOOK_FILE);
        List<Book> results = new ArrayList<>();

        if (choice.equals("1")) {
            System.out.print("Enter the book ID: ");
            int id = getIntInput();
            results = books.stream().filter(b -> b.getId() == id).collect(Collectors.toList());
        } else if (choice.equals("2")) {
            System.out.print("Enter Book Name: ");
            String name = scanner.nextLine();
            results = books.stream().filter(b -> b.getName().equalsIgnoreCase(name)).collect(Collectors.toList());
        } else {
            System.out.println("Invalid choice.");
        }

        if (results.isEmpty()) {
            System.out.println("No Record Found.");
        } else {
            System.out.println("\n--- Search Results ---");
            System.out.printf("%-15s %-7s %-20s %-20s %-7s %-10s %-7s%n", 
                "CATEGORY", "ID", "BOOK NAME", "AUTHOR", "QTY", "PRICE", "RACK NO");
            System.out.println("---------------------------------------------------------------------------------------------");
            results.forEach(System.out::println);
        }
        pressEnterToContinue();
    }
    
    /**
     * Displays the menu for issuing books.
     */
    private static void issueMenu() {
        clearConsole();
        System.out.println("--------------- ISSUE SECTION ---------------");
        System.out.println("1. Issue a Book");
        System.out.println("2. View Issued Books");
        System.out.println("3. Remove Issued Book");
        System.out.print("Enter a Choice: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1": issueBook(); break;
            case "2": viewIssuedBooks(); break;
            case "3": removeIssuedBook(); break;
            default: System.out.println("Invalid choice."); pressEnterToContinue(); break;
        }
    }

    /**
     * Issues a book to a student.
     */
    private static void issueBook() {
        clearConsole();
        System.out.println("--------------- Issue Book ---------------");
        System.out.print("Enter the Book ID: ");
        int id = getIntInput();

        List<Book> libraryBooks = readFromFile(BOOK_FILE);
        Book bookToIssue = null;
        int bookIndex = -1;

        for (int i = 0; i < libraryBooks.size(); i++) {
            if (libraryBooks.get(i).getId() == id) {
                bookToIssue = libraryBooks.get(i);
                bookIndex = i;
                break;
            }
        }

        if (bookToIssue != null) {
            if (bookToIssue.getQuantity() > 0) {
                System.out.println("Book Found: " + bookToIssue.getName());
                System.out.print("Enter student name: ");
                String studentName = scanner.nextLine();

                bookToIssue.issueBook(studentName, RETURN_TIME);
                bookToIssue.setQuantity(bookToIssue.getQuantity() - 1);
                
                // Update the quantity in the main library file
                libraryBooks.set(bookIndex, bookToIssue);
                writeToFile(BOOK_FILE, libraryBooks);

                // Add a record to the issued books file
                List<Book> issuedBooks = readFromFile(ISSUE_FILE);
                issuedBooks.add(bookToIssue);
                writeToFile(ISSUE_FILE, issuedBooks);
                
                System.out.println("\nBook issued successfully.");
                System.out.println("To be returned by: " + bookToIssue.getDueDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

            } else {
                System.out.println("Book is out of stock.");
            }
        } else {
            System.out.println("No record found for this ID in the library.");
        }
        pressEnterToContinue();
    }

    /**
     * Displays a list of all currently issued books.
     */
    private static void viewIssuedBooks() {
        clearConsole();
        System.out.println("------------------------------------- Issued Book List -------------------------------------");
        List<Book> issuedBooks = readFromFile(ISSUE_FILE);

        if (issuedBooks.isEmpty()) {
            System.out.println("No books are currently issued.");
        } else {
            System.out.printf("%-20s %-15s %-7s %-20s %-15s %-15s%n",
                "STUDENT NAME", "CATEGORY", "ID", "BOOK NAME", "ISSUED DATE", "RETURN DATE");
            System.out.println("----------------------------------------------------------------------------------------------------");
            issuedBooks.forEach(b -> System.out.println(b.toIssuedString()));
        }
        pressEnterToContinue();
    }
    
    /**
     * Removes a book record from the issued list (book return).
     */
    private static void removeIssuedBook() {
        clearConsole();
        System.out.println("--------------- Return a Book ---------------");
        System.out.print("Enter the Book ID to return: ");
        int id = getIntInput();

        List<Book> issuedBooks = readFromFile(ISSUE_FILE);
        Book returnedBook = null;
        for (Book book : issuedBooks) {
            if (book.getId() == id) {
                returnedBook = book;
                break;
            }
        }

        if (returnedBook != null) {
            // Remove from issued list
            issuedBooks.remove(returnedBook);
            writeToFile(ISSUE_FILE, issuedBooks);

            // Increment quantity in the main library
            List<Book> libraryBooks = readFromFile(BOOK_FILE);
            for (int i = 0; i < libraryBooks.size(); i++) {
                if (libraryBooks.get(i).getId() == id) {
                    Book libBook = libraryBooks.get(i);
                    libBook.setQuantity(libBook.getQuantity() + 1);
                    libraryBooks.set(i, libBook);
                    break;
                }
            }
            writeToFile(BOOK_FILE, libraryBooks);

            System.out.println("Book has been successfully returned.");
        } else {
            System.out.println("No issued book found with that ID.");
        }
        pressEnterToContinue();
    }
    
    /**
     * Displays all books available in the library.
     */
    private static void viewBooks() {
        clearConsole();
        System.out.println("-------------------------------------- Book List --------------------------------------");
        List<Book> books = readFromFile(BOOK_FILE);
        if (books.isEmpty()) {
            System.out.println("The library is empty.");
        } else {
            System.out.printf("%-15s %-7s %-20s %-20s %-7s %-10s %-7s%n", 
                "CATEGORY", "ID", "BOOK NAME", "AUTHOR", "QTY", "PRICE", "RACK NO");
            System.out.println("---------------------------------------------------------------------------------------------");
            int totalBooks = 0;
            for (Book book : books) {
                System.out.println(book);
                totalBooks += book.getQuantity();
            }
            System.out.println("---------------------------------------------------------------------------------------------");
            System.out.println("Total Books = " + totalBooks);
        }
        pressEnterToContinue();
    }
    
    /**
     * Edits the records of an existing book.
     */
    private static void editBooks() {
        clearConsole();
        System.out.println("--------------- Edit Books Section ---------------");
        System.out.print("Enter Book ID to be edited: ");
        int id = getIntInput();

        List<Book> books = readFromFile(BOOK_FILE);
        Book bookToEdit = null;
        int bookIndex = -1;

        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId() == id) {
                bookToEdit = books.get(i);
                bookIndex = i;
                break;
            }
        }

        if (bookToEdit != null) {
            System.out.println("Book Found. Current details: " + bookToEdit.getName());
            System.out.print("Enter new name (or press Enter to keep '" + bookToEdit.getName() + "'): ");
            String newName = scanner.nextLine();
            if (!newName.isEmpty()) bookToEdit.setName(newName);

            System.out.print("Enter new Author (or press Enter to keep '" + bookToEdit.getAuthor() + "'): ");
            String newAuthor = scanner.nextLine();
            if (!newAuthor.isEmpty()) bookToEdit.setAuthor(newAuthor);

            System.out.print("Enter new quantity (or press Enter to keep '" + bookToEdit.getQuantity() + "'): ");
            String newQtyStr = scanner.nextLine();
            if (!newQtyStr.isEmpty()) bookToEdit.setQuantity(Integer.parseInt(newQtyStr));

            System.out.print("Enter new price (or press Enter to keep '" + bookToEdit.getPrice() + "'): ");
            String newPriceStr = scanner.nextLine();
            if (!newPriceStr.isEmpty()) bookToEdit.setPrice(Double.parseDouble(newPriceStr));
            
            System.out.print("Enter new rack number (or press Enter to keep '" + bookToEdit.getRackNo() + "'): ");
            String newRackStr = scanner.nextLine();
            if (!newRackStr.isEmpty()) bookToEdit.setRackNo(Integer.parseInt(newRackStr));
            
            books.set(bookIndex, bookToEdit);
            writeToFile(BOOK_FILE, books);

            System.out.println("\nThe record has been modified.");
        } else {
            System.out.println("No record found for that ID.");
        }
        pressEnterToContinue();
    }
    
    // ------------------ HELPER METHODS ------------------

    private static int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }

    private static double getDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }

    private static void pressEnterToContinue() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }

    private static void printCurrentTime() {
        System.out.println("Date and time: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
    }

    private static Book findBookById(int id, String filename) {
        List<Book> books = readFromFile(filename);
        for (Book book : books) {
            if (book.getId() == id) {
                return book;
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private static List<Book> readFromFile(String filename) {
        List<Book> books = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) {
            return books; // Return empty list if file doesn't exist yet
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            books = (List<Book>) ois.readObject();
        } catch (EOFException e) {
            // This is fine, means file is empty
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        }
        return books;
    }

    private static void writeToFile(String filename, List<Book> books) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(books);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}