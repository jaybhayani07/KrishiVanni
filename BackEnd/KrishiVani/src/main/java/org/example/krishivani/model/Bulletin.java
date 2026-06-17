package org.example.krishivani.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bulletins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bulletin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String titleGu;
    private String titleEn;
    private String tagText;
    private String tagColorHex;
    private String tagBgHex;
    private String dateStr;
    private String fileSizeStr;
}
