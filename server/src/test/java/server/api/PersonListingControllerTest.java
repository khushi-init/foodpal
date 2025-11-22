/**
 * Copyright 2024 Sebastian Proksch
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package server.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commons.Person;

public class PersonListingControllerTest {

    private static final Person MICKEY = new Person("Mickey", "Mouse");
    private static final Person DONALD = new Person("Donald", "Duck");
    private static final Person SCROOGE = new Person("Scrooge", "McDuck");

    private PersonListingController sut;

    @BeforeEach
    public void setup() {
        sut = new PersonListingController();
    }

    @Test
    public void containsTwoDefaultNames() {
        List<Person> actual = sut.list();
        List<Person> expected = List.of(MICKEY, DONALD);
        assertEquals(expected, actual);
    }

    @Test
    public void canAddPeople() {
        List<Person> actual = sut.add(SCROOGE);
        List<Person> expected = List.of(MICKEY, DONALD, SCROOGE);
        assertEquals(expected, actual);
    }
}