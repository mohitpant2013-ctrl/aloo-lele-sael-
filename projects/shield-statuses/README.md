# Shield Statuses 1.1.0 - Forge 1.16.5 (36.2.39)

Green shield = ready to block, red shield = disabled. Works for you and for other players,
in singleplayer, LAN and on servers (also 1.21 servers through ViaVersion). Client side only.

- Your shield: taken from the shield cooldown.
- Other players: red for 5 s when the shield-break sound is played at them, or when you hit
  them with an axe while they block (note: no check that you hit from the front).
- If the server never sends the shield-break sound, the mod plays the vanilla sound itself.

Config file (after first start): `config/shieldstatuses-client.toml`
(colours, `interpolate` = fade red->green, `showOtherPlayers`, `showInGui`).

Build note: `ShieldRendererHook` (the drawing code) is kept as a compiled class in `prebuilt/`.
Because of that, `runClient` in a dev environment does not work - use the built jar in a normal
1.16.5 Forge instance.
