@echo off
REM Fusionner tous les fichiers Markdown dans l’ordre
type 00_*.md 01_*.md 02_*.md 03_*.md 04_*.md 05_*.md 06_*.md 07_*.md 08_*.md 09_*.md 10_*.md 11_*.md 12_*.md 13_*.md > arc42_full.md

REM Générer le PDF avec Pandoc
pandoc arc42_full.md -o arc42_documentation.pdf  --pdf-engine=wkhtmltopdf

echo PDF généré avec succès : arc42_documentation.pdf
pause
