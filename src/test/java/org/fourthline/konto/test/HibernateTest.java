package org.fourthline.konto.test;

import org.hibernate.Session;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.seamless.util.jpa.HibernateUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

/**
 * @author Christian Bauer
 */
public class HibernateTest {

    @BeforeSuite
    protected void setup() {
        HibernateUtil.getSessionFactory();
    }

    @BeforeMethod
    protected void beforeMethod() {
        new SchemaExport(HibernateUtil.getConfiguration()).drop(false, true);
        new SchemaExport(HibernateUtil.getConfiguration()).create(false, true);
    }

    protected Session getCurrentSession() {
        return HibernateUtil.getSessionFactory().getCurrentSession();
    }


}
