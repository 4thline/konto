package org.fourthline.konto.test;

import org.seamless.util.io.IO;
import org.fourthline.konto.server.importer.moneydance.MoneyDanceImporter;
import org.fourthline.konto.server.dao.AccountDAO;
import org.fourthline.konto.server.dao.CurrencyDAO;
import org.fourthline.konto.server.dao.EntryDAO;
import org.seamless.gwt.validation.shared.ValidationError;

import java.io.File;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class MoneyDanceImportTest extends HibernateTest {

    //@Test
    public void testImport() throws Exception {

        getCurrentSession().beginTransaction();

        String xml = IO.readLines(new File("moneydance.xml"));


        MoneyDanceImporter mdImporter =
                new MoneyDanceImporter(
                        new CurrencyDAO(),
                        new AccountDAO(),
                        new EntryDAO()
                );

        List<ValidationError> errors = mdImporter.importXML(xml);

        for (ValidationError error : errors) {
            System.out.println(error);
        }
        
        assertEquals(errors.size(), 0);

        getCurrentSession().getTransaction().commit();
    }
}
