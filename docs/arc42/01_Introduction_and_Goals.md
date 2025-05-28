# 1. Introduction et Objectifs

Ce document décrit les exigences et les objectifs qui guident le développement du système de caisse (POS) réalisé dans le cadre du laboratoire 1 du cours LOG430.

Ce laboratoire a pour but de consolider les concepts fondamentaux liés aux architectures logicielles simples. L’objectif est de concevoir et de développer une application client/serveur à deux niveaux (2-tier), dans laquelle :

- le **client** est une application console Java exécutée localement,
- le **serveur** est une base de données locale (SQLite) accessible directement.

L’application cible est un système de caisse pour un petit magasin de quartier, simple, robuste et autonome, qui pourra évoluer dans les futurs laboratoires (ex. : gestion multi-succursales, e-commerce distribué).

## 1.1. Vue d’ensemble des exigences

L'application est un système de caisse (POS) local, déployé sur une machine virtuelle (VM), destiné à un petit magasin de quartier.  
Les fonctionnalités principales incluent :

- la recherche de produits (par nom, identifiant ou catégorie)  
- l’enregistrement de ventes  
- la gestion des retours  
- la consultation de l’état du stock  

Le système doit fonctionner via une interface console, sans serveur HTTP, dans une architecture à deux niveaux (2-tier) avec une base de données locale.

## 1.2. Objectifs de qualité

1. **Simple** : l'aplication ne doit pas être complexe. 
2. **Robuste** : l'application doit fonctionner de façon stable même en cas de problèmes.
3. **Autonome** : le système doit fonctionner localement sans dépendre d'un réseau.

## 1.3. Parties prenantes

| Rôle                  | Nom               | Intérêt                                                              |
|-----------------------|-------------------|----------------------------------------------------------------------|
| Étudiant  | Vu Minh Vu-Le     | Conçoit, implémente et documente l’architecture du système          |
| Enseignant            | Fabio Petrillo    | Évalue la rigueur de l’architecture et de la documentation produite |
| Chargé de laboratoire | Hakim Ghlissi     | Fournit un encadrement technique et un accompagnement pédagogique   |
