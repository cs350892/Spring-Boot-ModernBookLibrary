package com.example.booklibrary;

import com.example.booklibrary.controller.BookController;
import com.example.booklibrary.entity.Book;
import com.example.booklibrary.repository.BookRepository;
import com.example.booklibrary.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookLibraryApplicationTests {

    @Autowired
    private BookController bookController;

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    @Test
    void contextLoads() {
        assertNotNull(bookController);
        assertNotNull(bookService);
        assertNotNull(bookRepository);
    }

    @Test
    void testCreateAndFetchBook() {
        Book book = new Book("Clean Code", "Robert C. Martin", "978-0132350884", 450L, 2008);
        ResponseEntity<Book> response = bookController.addBook(book);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Clean Code", response.getBody().getTitle());
        assertEquals("Robert C. Martin", response.getBody().getAuthor());

        // Get all books
        List<Book> allBooks = bookController.getAllBooks();
        assertEquals(1, allBooks.size());
        assertEquals("Clean Code", allBooks.get(0).getTitle());

        // Get by ID
        ResponseEntity<Book> byId = bookController.getBookById(response.getBody().getId());
        assertEquals(HttpStatus.OK, byId.getStatusCode());
        assertNotNull(byId.getBody());
        assertEquals("978-0132350884", byId.getBody().getIsbn());

        // Get by ISBN
        ResponseEntity<Book> byIsbn = bookController.getBookByIsbn("978-0132350884");
        assertEquals(HttpStatus.OK, byIsbn.getStatusCode());
        assertNotNull(byIsbn.getBody());
        assertEquals("Clean Code", byIsbn.getBody().getTitle());

        // Get by Author
        List<Book> byAuthor = bookController.getBooksByAuthor("Robert C. Martin");
        assertEquals(1, byAuthor.size());
        assertEquals("Robert C. Martin", byAuthor.get(0).getAuthor());

        // Get books cheaper than 500
        List<Book> cheaperBooks = bookController.getBooksCheaperThan(500L);
        assertEquals(1, cheaperBooks.size());

        // Get books cheaper than 400 (should be empty)
        List<Book> evenCheaper = bookController.getBooksCheaperThan(400L);
        assertEquals(0, evenCheaper.size());
    }

    @Test
    void testUpdateAndDeleteBook() {
        Book savedBook = bookService.addBook(new Book("Spring in Action", "Craig Walls", "978-1617294945", 500L, 2018));
        Long bookId = savedBook.getId();

        // Update book
        Book updatedData = new Book("Spring in Action 6th Edition", "Craig Walls", "978-1617294945", 550L, 2022);
        ResponseEntity<Book> updateResponse = bookController.updateBook(bookId, updatedData);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals("Spring in Action 6th Edition", updateResponse.getBody().getTitle());
        assertEquals(550L, updateResponse.getBody().getPrice());
        assertEquals(2022, updateResponse.getBody().getPublishedYear());

        // Delete book
        ResponseEntity<String> deleteResponse = bookController.deleteBook(bookId);
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertEquals("Book deleted successfully", deleteResponse.getBody());

        // Confirm deleted
        ResponseEntity<Book> notFoundResponse = bookController.getBookById(bookId);
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());
    }

    @Test
    void testNotFoundScenarios() {
        ResponseEntity<Book> notFoundById = bookController.getBookById(9999L);
        assertEquals(HttpStatus.NOT_FOUND, notFoundById.getStatusCode());

        ResponseEntity<Book> notFoundByIsbn = bookController.getBookByIsbn("non-existent-isbn");
        assertEquals(HttpStatus.NOT_FOUND, notFoundByIsbn.getStatusCode());

        ResponseEntity<Book> notFoundUpdate = bookController.updateBook(9999L, new Book("Title", "Author", "ISBN", 100L, 2020));
        assertEquals(HttpStatus.NOT_FOUND, notFoundUpdate.getStatusCode());

        ResponseEntity<String> notFoundDelete = bookController.deleteBook(9999L);
        assertEquals(HttpStatus.NOT_FOUND, notFoundDelete.getStatusCode());
    }
}
