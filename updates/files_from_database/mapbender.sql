UPDATE mapbender.gui_element
SET e_content = replace(e_content, 'ks_${id}_normal.jpg', '${foto_normal}')
WHERE e_content ilike '%ks_${id}_normal.jpg%'
