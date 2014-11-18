echo "Führe add_columns.sql in DB klarschiff_backend aus:"
psql -f add_columns.sql klarschiff_backend klarschiff_backend

echo "Führe migrate_data.sql in DB klarschiff_backend aus:"
psql -f migrate_data.sql klarschiff_backend klarschiff_backend 

echo "Führe frontend_db.sql in DB klarschiff_frontend aus:"
psql -f frontend_db.sql klarschiff_frontend klarschiff_frontend

echo "Führe mapbender.sql in DB mapbender aus:"
psql -f mapbender.sql mapbender mapbender
