package ru.levelp.java.junior.haw;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class HibernateTest {
    private EntityManagerFactory emf;
    private EntityManager em;

    @Before
    public void setUp() throws Exception {
        emf = Persistence.createEntityManagerFactory("NewPersistenceUnit");
        em = emf.createEntityManager();
    }

    @After
    public void tearDown() throws Exception {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }

    @Test
    public void testUser() throws Exception {
        em.getTransaction().begin();

        User user = new User();
        user.setLogin("root");
        user.setBalance(1.0);

        em.persist(user);

        em.getTransaction().commit();

        assertEquals("root", em.find(User.class, user.getUserId()).getLogin());
    }

    @Test
    public void testTransactions() throws Exception {
        em.getTransaction().begin();

        User user = new User();
        user.setLogin("root");
        user.setBalance(1.0);

        em.persist(user);

        Transaction t = new Transaction();
        t.setDate(new Date());
        t.setAmount(10);
        t.setUser(user);
        t.setTarget(user);

        em.persist(t);

        user.setBalance(10.0);

        em.getTransaction().commit();

        em.refresh(user);

        List<Transaction> transactions = user.getTransactions();
        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals(t, transactions.get(0));
    }

    @Test
    public void testEnsureRootUSer() throws Exception {
        User user = new MoneyFacadeDAO(em).ensureRootUser();
        User user2 = new MoneyFacadeDAO(em).ensureRootUser();

        assertSame(user, user2);
    }

    @Test
    public void testEmitMoney() throws Exception {
        MoneyFacadeDAO dao = new MoneyFacadeDAO(em);
        dao.ensureRootUser();

        dao.emitMoney(10.0);

        assertEquals(10.0, dao.findUser(User.RootUserName).getBalance(), 0.01);
        assertEquals(10.0, dao.findUser(User.RootUserName).getTransactions().get(0).getAmount(), 0.01);

        dao.emitMoney(7.0);

        assertEquals(17.0, dao.findUser(User.RootUserName).getBalance(), 0.01);
        assertEquals(7, dao.findUser(User.RootUserName).getTransactions().get(1).getAmount(), 0.01);
    }
}
