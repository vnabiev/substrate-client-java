package com.strategyobject.substrateclient.types;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {


    @Test
    void isOk() {
        assertTrue(Result.ok(1).isOk());
        assertFalse(Result.err(1).isOk());
    }

    @Test
    void isErr() {
        assertFalse(Result.ok(1).isErr());
        assertTrue(Result.err(1).isErr());
    }

    @Test
    void ok() {
        assertEquals(Optional.of(1), Result.ok(1).ok());
        assertEquals(Optional.empty(), Result.err(1).ok());
    }

    @Test
    void err() {
        assertEquals(Optional.of(1), Result.err(1).err());
        assertEquals(Optional.empty(), Result.ok(1).err());
    }

    @Test
    void map() {
        assertEquals(Result.ok(2), Result.ok(1).map(i -> i + 1));
        assertEquals(Result.err(1), Result.<Integer, Integer>err(1).map(i -> i + 1));
    }

    @Test
    void mapOr() {
        assertEquals(Integer.valueOf(2), Result.<Integer, Integer>ok(1).mapOr(0, i -> i + 1));
        assertEquals(Integer.valueOf(0), Result.<Integer, Integer>err(1).mapOr(0, i -> i + 1));
    }

    @Test
    void mapOrElse() {
        assertEquals(Integer.valueOf(2), Result.<Integer, Integer>ok(1).mapOrElse(i -> i - 1, i -> i + 1));
        assertEquals(Integer.valueOf(0), Result.<Integer, Integer>err(1).mapOrElse(i -> i - 1, i -> i + 1));
    }

    @Test
    void mapErr() {
        assertEquals(Result.ok(1), Result.<Integer, Integer>ok(1).mapErr(i -> i + 1));
        assertEquals(Result.err(2), Result.<Integer, Integer>err(1).mapErr(i -> i + 1));
    }

    @Test
    void and() {
        assertEquals(Result.ok(2), Result.ok(1).and(Result.ok(2)));
        assertEquals(Result.err(1), Result.err(1).and(Result.ok(2)));
    }

    @Test
    void andThen() {
        assertEquals(Result.ok(2), Result.ok(1).andThen(i -> Result.ok(i + 1)));
        assertEquals(Result.err(1), Result.<Integer, Integer>err(1).andThen(i -> Result.ok(i + 1)));
    }

    @Test
    void or() {
        assertEquals(Result.ok(1), Result.ok(1).or(Result.ok(2)));
        assertEquals(Result.ok(2), Result.err(1).or(Result.ok(2)));
    }

    @Test
    void orElse() {
        assertEquals(Result.ok(1), Result.ok(1).orElse(i -> Result.ok(2)));
        assertEquals(Result.ok(2), Result.err(1).orElse(i -> Result.ok(2)));
    }

    @Test
    void unwrapOr() {
        assertEquals(Integer.valueOf(1), Result.ok(1).unwrapOr(2));
        assertEquals(Integer.valueOf(2), Result.<Integer, Integer>err(1).unwrapOr(2));
    }

    @Test
    void unwrapOrElse() {
        assertEquals(Integer.valueOf(1), Result.ok(1).unwrapOrElse(i -> 2));
        assertEquals(Integer.valueOf(2), Result.<Integer, Integer>err(1).unwrapOrElse(i -> 2));
    }

    @Test
    void unwrap() {
        assertEquals(1, Result.ok(1).unwrap());
        assertThrows(NoSuchElementException.class, Result.err(1)::unwrap);
    }

    @Test
    void expect() {
        assertEquals(1, Result.ok(1).expect("no error"));
        assertThrows(NoSuchElementException.class, () -> Result.err(1).expect("error msg"), "error msg");
    }

    @Test
    void unwrapErr() {
        assertEquals(1, Result.err(1).unwrapErr());
        assertThrows(NoSuchElementException.class, Result.ok(1)::unwrapErr);
    }

    @Test
    void expectErr() {
        assertEquals(1, Result.err(1).expectErr("no error"));
        assertThrows(NoSuchElementException.class, () -> Result.ok(1).expectErr("error msg"), "error msg");
    }

    @Test
    void equals() {
        val a = Result.ok(1);
        val b = Result.ok(2);
        val c = Result.err(1);
        val d = Result.err(2);
        val e = Result.ok(1);
        val g = Result.err(1);
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, d);
        assertNotEquals(b, c);
        assertNotEquals(b, d);
        assertNotEquals(c, d);
        assertEquals(a, e);
        assertEquals(c, g);
    }

    @Test
    void testHashCode() {
        val a = Result.ok(1);
        val b = Result.ok(2);
        val c = Result.err(1);
        val d = Result.err(2);
        assertNotEquals(a.hashCode(), b.hashCode());
        assertEquals(a.hashCode(), c.hashCode());
        assertNotEquals(a.hashCode(), d.hashCode());
        assertNotEquals(b.hashCode(), c.hashCode());
        assertEquals(b.hashCode(), d.hashCode());
        assertNotEquals(c.hashCode(), d.hashCode());
    }

    @Test
    void testToString() {
        assertEquals("Ok(1)", Result.ok(1).toString());
        assertEquals("Err(2)", Result.err(2).toString());
    }
}
