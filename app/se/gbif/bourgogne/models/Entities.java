/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.gbif.bourgogne.models;

import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
//import javax.persistence.NamedQueries;
//import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import play.db.ebean.*;
import play.db.ebean.Model.Finder;
import play.data.validation.Constraints.*;

import com.avaje.ebean.*;

/**
 *
 * @author korbinus
 */
@Entity
//@Table(name = "entities", uniqueConstraints = {
//    @UniqueConstraint(columnNames = {"id"}),
//    @UniqueConstraint(columnNames = {"originalUID"})})
//@XmlRootElement
//@NamedQueries({
//    @NamedQuery(name = "Entities.findAll", query = "SELECT e FROM Entities e"),
//    @NamedQuery(name = "Entities.findByAddedId", query = "SELECT e FROM Entities e WHERE e.addedId = :addedId"),
//    @NamedQuery(name = "Entities.findByUpdated", query = "SELECT e FROM Entities e WHERE e.updated = :updated"),
//    @NamedQuery(name = "Entities.findByDatasetid", query = "SELECT e FROM Entities e WHERE e.datasetid = :datasetid"),
//    @NamedQuery(name = "Entities.findByOriginalUID", query = "SELECT e FROM Entities e WHERE e.originalUID = :originalUID"),
//    @NamedQuery(name = "Entities.findByDeleted", query = "SELECT e FROM Entities e WHERE e.deleted = :deleted"),
//    @NamedQuery(name = "Entities.findByProcessed", query = "SELECT e FROM Entities e WHERE e.processed = :processed")})
public class Entities extends Model{
    private static final long serialVersionUID = 1L;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "added_id", nullable = false)
    public Integer addedId;
    // IMPORTANT provide this one as @Id otherwise Ebean Finder won't work
    @Id
    @Basic(optional = false)
    @NotNull
//    @Lob
    @Column(name = "id", nullable = false)
    public byte[] id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "updated", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date updated;
//    @Lob
    @Column(name = "body")
    public byte[] body;
    @Column(name = "datasetid")
    public Integer datasetid;
    @Size(max = 255)
    @Column(name = "originalUID", length = 255)
    public String originalUID;
    @Column(name = "deleted")
    public Boolean deleted;
    @Column(name = "processed")
    public Boolean processed;

	public static Finder<byte[], Entities> find = new Finder(byte[].class,
			Entities.class);    

	public static List<Entities> all() {
		return find.all();
	}	

	/**
	 * Delete an Entities
	 * @param id
	 */
	public static void delete(byte[] id) {
		find.ref(id).delete();
	}	
}
