package ru.edu.pgtk.weducation.entity;

import java.io.Serializable;
import javax.ejb.EJBException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import static ru.edu.pgtk.weducation.utils.Utils.getHash;

@Entity
@Table(name = "accounts")
public class Account implements Serializable {

  @Id
  @Column(name = "aco_pcode")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "aco_fullname", nullable = false, length = 255)
  private String fullName;
  
  @Column(name = "aco_login", nullable = false, length = 50)
  private String login;

  @Column(name = "aco_password", nullable = false, length = 50)
  private String passwordHash;
  
  @Column(name = "aco_role", nullable = false)
  private AccountRole role;
  
  @Column(name = "aco_code")
  private int code;

  @Transient
  private String password;

  @Transient
  private String confirm;

  public void updatePassword() {
    if ((password != null) && (!password.isEmpty())) {
      if (password.contentEquals(confirm)) {
        passwordHash = getHash(password);
      } else {
        throw new EJBException("Password and confirmation doesn't match!");
      }
    } else {
      throw new EJBException("Empty and null passwords not accepted!");
    }
  }
  
  public int getId() {
    return id;
  }
  
  public boolean isAdmin() {
    return role == AccountRole.ADMIN;
  }
  
  public boolean isDepartment() {
    return role == AccountRole.DEPARTMENT;
  }
  
  public String getRoleString() {
    return role.getDescription();
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getConfirm() {
    return confirm;
  }

  public void setConfirm(String confirm) {
    this.confirm = confirm;
  }

  public AccountRole getRole() {
    return role;
  }

  public void setRole(AccountRole role) {
    this.role = role;
  }
}
