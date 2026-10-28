package com.example.booklibrary.service;

import com.example.booklibrary.entity.Book;
import com.example.booklibrary.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    // Add Book
    public Book addBook(Book book) {
        return repository.save(book);
    }

    // Get all books
    public List<Book> getAllBooks() {
        return repository.findAll();
    }

    // Find book by ID
    public Optional<Book> getBookById(Long id) {
        return repository.findById(id);
    }

    // Find book by ISBN
    public Optional<Book> getBookByIsbn(String isbn) {
        return repository.findByIsbn(isbn);
    }

    // Find books by Author
    public List<Book> getBooksByAuthor(String author) {
        return repository.findByAuthor(author);
    }

    // Find books cheaper than price
    public List<Book> getBooksCheaperThan(Long price) {
        return repository.findByPriceLessThan(price);
    }

    // Update book
    public Optional<Book> updateBook(Long id, Book bookData) {
        return repository.findById(id).map(existing -> {
            existing.setTitle(bookData.getTitle());
            existing.setAuthor(bookData.getAuthor());
            existing.setIsbn(bookData.getIsbn());
            existing.setPrice(bookData.getPrice());
            existing.setPublishedYear(bookData.getPublishedYear());
            return repository.save(existing);
        });
    }

    // Delete book
    public boolean deleteBook(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
