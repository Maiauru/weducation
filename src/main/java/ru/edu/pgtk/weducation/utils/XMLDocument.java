package ru.edu.pgtk.weducation.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import static ru.edu.pgtk.weducation.utils.PlanParser.BLOCK_NOT_FOUND;

/**
 * Клас для инкапсуляции XML документа и кое-каких функций для работы с ним.
 *
 * @author Воронин Леонид
 */
public class XMLDocument {

  private Document document;  // XML документ

  public XMLDocument(InputStream stream) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      document = builder.parse(stream);
    } catch (IOException | ParserConfigurationException | SAXException | NullPointerException e) {
      throw new EJBException(e.getMessage());
    }
  }

  /**
   * Получает список коренных элементов (тегов) по имени.
   *
   * Элементы получаются посредством getElementsByTagName(tagName) и копируются
   * в ArrayList
   *
   * @param tagName имя тега
   * @return список элементов в виде NodeList
   */
  public List<Node> getRootNodes(String tagName) {
    try {
      List<Node> result = new ArrayList<>();
      NodeList nodes = document.getElementsByTagName(tagName);
      if (nodes.getLength() > 0) {
        for (int n = 0; n < nodes.getLength(); n++) {
          result.add(nodes.item(n));
        }
      } else {
        throw new EJBException(BLOCK_NOT_FOUND + tagName);
      }
      return result;
    } catch (NullPointerException e) {
      throw new EJBException(BLOCK_NOT_FOUND + tagName);
    }
  }

  /**
   * Возвращает один элемент с указаным именем.
   *
   * Метод получает все элементы документа с указаным именем и возвращает первый
   * из них.
   *
   * @param tagName имя тега
   * @return экземпляр класса Node
   */
  public Node getRootNode(String tagName) {
    try {
      NodeList nodes = document.getElementsByTagName(tagName);
      if (nodes.getLength() > 0) {
        return nodes.item(0);
      } else {
        throw new EJBException(BLOCK_NOT_FOUND + tagName);
      }
    } catch (NullPointerException e) {
      throw new EJBException(BLOCK_NOT_FOUND + tagName);
    }
  }

  /**
   * Возвращает список дочерних элементов с указанным именем
   *
   * @param node элемент, среди дочерних элементов которого проводится поиск
   * @param tagName имя дочернего элемента
   * @return список экземпляров класса Node
   */
  public List<Node> getChildNodes(Node node, String tagName) {
    try {
      List<Node> result = new ArrayList<>();
      if (node.hasChildNodes()) {
        // Если есть дочерние элементы, то получаем их
        NodeList nodes = node.getChildNodes();
        for (int n = 0; n < nodes.getLength(); n++) {
          // Пробегаем по ним циклом и отбираем только те, имя которых соответствует требуемому
          if (nodes.item(n).getNodeName().contentEquals(tagName)) {
            result.add(nodes.item(n));
          }
        }
      }
      return result;
    } catch (NullPointerException e) {
      throw new EJBException("NullPointerException при поиске дочерних элементов с именем " + tagName);
    }
  }

  public Node getChildNode(Node node, String tagName) {
    try {
      if (node.hasChildNodes()) {
        // Если есть дочерние элементы, то получаем их
        NodeList nodes = node.getChildNodes();
        for (int n = 0; n < nodes.getLength(); n++) {
          // Пробегаем по ним циклом и отбираем только те, имя которых соответствует требуемому
          if (nodes.item(n).getNodeName().contentEquals(tagName)) {
            return nodes.item(n);
          }
        }
      }
      return null;
    } catch (NullPointerException e) {
      throw new EJBException("NullPointerException при поиске дочерних элементов с именем " + tagName);
    }
  }
  
  
  /**
   * Возвращает значение атрибута тега по имени атрибута
   *
   * @param node тег в виде экземпляра класса Node
   * @param attributeName наименование атрибута
   * @return Значение атрибута в виде строки
   */
  public String getAttributeValue(Node node, String attributeName) {
    try {
      if (node.hasAttributes()) {
        NamedNodeMap attributes = node.getAttributes();
        for (int a = 0; a < attributes.getLength(); a++) {
          if (attributes.item(a).getNodeName().contentEquals(attributeName)) {
            return attributes.item(a).getNodeValue();
          }
        }
      }
      return null;
    } catch (NullPointerException e) {
      throw new EJBException("NullPointerException при попытке получения значения атрибута " + attributeName);
    }
  }

  /**
   * Проверяет наличие атрибута с указанным именем.
   *
   * @param node Элемент, у которого проверяется наличие атрибута
   * @param attributeName имя атрибута
   * @return истина тогда в случае, если атрибут с таким именем присутствует.
   * Иначе - ложь.
   */
  public boolean attributeExists(Node node, String attributeName) {
    try {
      if (node.hasAttributes()) {
        NamedNodeMap attributes = node.getAttributes();
        for (int a = 0; a < attributes.getLength(); a++) {
          if (attributes.item(a).getNodeName().contentEquals(attributeName)) {
            return true;
          }
        }
      }
      return false;
    } catch (NullPointerException e) {
      throw new EJBException("NullPointerException при попытке получения значения атрибута " + attributeName);
    }
  }
}
